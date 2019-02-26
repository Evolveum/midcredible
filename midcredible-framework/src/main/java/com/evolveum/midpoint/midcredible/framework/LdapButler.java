package com.evolveum.midpoint.midcredible.framework;

import com.evolveum.midpoint.midcredible.framework.util.ComparatorImpl;
import com.evolveum.midpoint.midcredible.framework.util.Diff;
import com.evolveum.midpoint.midcredible.framework.util.QueryBuilder;
import com.evolveum.midpoint.midcredible.framework.util.structural.Attribute;
import com.evolveum.midpoint.midcredible.framework.util.structural.Identity;
import com.evolveum.midpoint.midcredible.framework.util.structural.Statistics;
import com.evolveum.midpoint.test.ldap.OpenDJController;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.CursorLdapReferralException;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapButler extends ResourceButler<LdapNetworkConnection> {

    private static OpenDJController openDJController = new OpenDJController();

    private static final Logger LOG = LoggerFactory.getLogger(LdapButler.class);

    private String baseContext;
    private String uidAttr;
    private static final String DEFAULT_UID_NAME = "dn";
    private static final String DEFAULT_OBJECT_CLASS_ALL = "(objectclass=*)";
    private ComparatorImpl comparatorImpl;

    public LdapButler(String id, Context context, String resourceOid) {
        super(id, context, resourceOid);
    }

    public LdapButler(String id, Context context, LdapNetworkConnection client) {
        super(id, context, client);
    }

   // @Override
    public ComparatorImpl compare() {

//			SearchScope scope = null;
//
//			Map<String, Object> opMap = queryBuilder.getOperationalAttributes();
//
//			if(opMap !=null && !opMap.isEmpty()){
//
//				for(String attrName: opMap.keySet()){
//					if ("scope".equals(attrName)){
//
////TODO
//					}else if ("size".equals(attrName)){
//
////TODO
//					}else if ("attributesToGet".equals(attrName)){
////TODO
//					}
//				}
//			}
//
//			String filter = translateToLdapQuery(queryBuilder);
//			if (scope!=null){
//			}else{
//				scope=SearchScope.SUBTREE;
//			}
//
//			return search(filter,scope,attrsToReturn);

        comparatorImpl = new ComparatorImpl(this);
        return comparatorImpl;
    }

   // @Override
    protected void executeComparison(Statistics statistics) {
        if (comparatorImpl != null) {

            LdapSearchResult ldapSearchResult = new LdapSearchResult();

            //pagedSearch();

            try {
                cleanClient();
            } catch (LdapException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected LdapNetworkConnection init() throws Exception {
        ConfigurationPropertiesType config = getConfigurationProperties();

        String host = getValue(config, "host");
        String port = getValue(config, "port");
        String bindDn = getValue(config, "bindDn");
        String bindPassword = getValue(config, "bindPassword");
        String timeout = getValue(config, "connectTimeout");
        uidAttr = getValue(config, "uidAttribute");
        if (StringUtils.isEmpty(timeout)) {
            timeout = "10000";
        }

        baseContext = getValue(config, "baseContext");

        String connectionSecurity = getValue(config, "connectionSecurity");

        LdapConnectionConfig lcc = new LdapConnectionConfig();
        lcc.setLdapHost(host);
        lcc.setLdapPort(Integer.parseInt(port));
        lcc.setTimeout(Integer.parseInt(timeout));

        if (connectionSecurity == null || "none".equals(connectionSecurity)) {
            // Nothing to do
        } else if ("ssl".equals(connectionSecurity)) {
            lcc.setUseSsl(true);
        } else if ("starttls".equals(connectionSecurity)) {
            lcc.setUseTls(true);
        }

        LdapNetworkConnection con = new LdapNetworkConnection(lcc);
        con.connect();

        try {
            LOG.debug("Attempting bind to host: " + host + " port: " + port);
            con.bind(bindDn, bindPassword);
        } catch (Exception ex) {
            con.close();
            throw ex;
        }

        return con;
    }

    public String getBaseContext() {
        return baseContext;
    }

    public void setBaseContext(String baseContext) {
        this.baseContext = baseContext;
    }

    public static OpenDJController getOpenDJController() {
        return openDJController;
    }

    public void cleanStart() throws IOException, URISyntaxException {
        openDJController.startCleanServer();
    }

    /**
     * Starts embedded instance
     */
    public void start() {
        openDJController.start();
    }

    /**
     * Stops embedded instance
     */
    public void stop() {
        openDJController.stop();
    }

//	public List<Identity> search(QueryBuilder query, SearchScope scope, String... attrsToReturn) throws CursorException, LdapException {
//
//		String filter = translateToLdapQuery(query);
//		if (scope!=null){
//		}else{
//			scope=SearchScope.SUBTREE;
//		}
//
//	return search(filter,scope,attrsToReturn);
//	}

    private List<Identity> search(String filter, SearchScope scope, String... attrsToReturn) throws LdapException, CursorException {
        LOG.debug("Formulating search query using the following scope: " + scope.toString() + " filter: "
                + System.lineSeparator() + filter + System.lineSeparator() + " to return the following attributes: " + attrsToReturn);

        SearchRequest searchRequest = new SearchRequestImpl();
        searchRequest.setBase(new Dn(getBaseContext()));
        searchRequest.setFilter(filter);
        searchRequest.setScope(scope);
        searchRequest.addAttributes(attrsToReturn);
        searchRequest.ignoreReferrals();

        List<Entry> entries = new ArrayList<Entry>();

        try {
            SearchCursor searchCursor = getClient().search(searchRequest);
            while (searchCursor.next()) {
                Response response = searchCursor.get();
                if (response instanceof SearchResultEntry) {
                    Entry entry = ((SearchResultEntry) response).getEntry();
                    entries.add(entry);
                }
            }
            searchCursor.close();
        } catch (IOException e) {
            throw new IllegalStateException("IO Error: " + e.getMessage(), e);
        } catch (CursorLdapReferralException e) {
            throw new IllegalStateException("Got referral to: " + e.getReferralInfo(), e);
        }

        return remapToIdentity(entries);
    }

    private List<Identity> remapToIdentity(List<Entry> entries) {

        List<Identity> identities = new ArrayList<>();

        entries.forEach(entry -> {


            String uid = "";

            if (uidAttr != null && !(uidAttr.isEmpty())) {

                uid = entry.get(uidAttr).get().getValue();
            } else {
                uid = entry.get(DEFAULT_UID_NAME).get().getValue();
            }

            if (uid != null) {

                List<Attribute> attrs = new ArrayList<>();

                entry.getAttributes().forEach(attr -> {
                    Attribute attribute = new Attribute(getId(), getResouceOid(), attr.getId());
                    List<Object> values = new ArrayList<>();
                    attr.forEach(value -> {

                        values.add(value.getValue());

                    });

                    Map<Diff, Collection<Object>> stateAndValue = new HashMap<>();
                    stateAndValue.put(Diff.NONE, values);

                    attribute.setValues(stateAndValue);

                    attrs.add(attribute);

                });

                Identity identity = new Identity(uid, null, getId(), attrs);
                identities.add(identity);
            }
        });

        return identities;
    }

    private String translateToLdapQuery(QueryBuilder query) {

        return null;
    }

    private LdapSearchResult pagedSearch(String filter, Integer size, SearchScope searchScope, LdapSearchResult searchResult) throws LdapException, IOException {

        if (filter != null && !(filter.isEmpty())) {
        } else {
            filter = "(ObjectClass=*)";
        }

        PagedResults pagedSearchControl = new PagedResultsImpl();
        pagedSearchControl.setSize(size);
        pagedSearchControl.setCookie(searchResult.getCookie());
        pagedSearchControl.setCritical(true);

        List<Entry> entries = new ArrayList<Entry>();
        SearchCursor searchCursor = null;

        try {
            SearchRequest searchRequest = new SearchRequestImpl();
            searchRequest.setBase(new Dn(getBaseContext()));
            searchRequest.setFilter(filter);
            searchRequest.setScope(searchScope);
            searchRequest.addAttributes("*");
            searchRequest.addControl(pagedSearchControl);


            searchCursor = getClient().search(searchRequest);
            int i = 0;

            while (searchCursor.next()) {
                Response response = searchCursor.get();
                if (response instanceof SearchResultEntry) {
                    Entry entry = ((SearchResultEntry) response).getEntry();
                    entries.add(entry);
                }
            }

            SearchResultDone result = searchCursor.getSearchResultDone();
            pagedSearchControl = (PagedResults) result.getControl(PagedResults.OID);


            if (result.getLdapResult().getResultCode() == ResultCodeEnum.UNWILLING_TO_PERFORM) {
                LOG.error("The server reported an error: " + ResultCodeEnum.UNWILLING_TO_PERFORM);

            }
        } catch (CursorException e) {

            LOG.error("And unexpected exception: " + e.getLocalizedMessage());
        } finally {
            if (searchCursor != null) {
                searchCursor.close();
            }
        }

        byte[] cookie = pagedSearchControl.getCookie();


        if (Strings.isEmpty(cookie)) {
            LOG.debug("Cookie returned by paged search is empty.");
        }

        searchResult.setCookie(cookie);
        searchResult.setIdentities(remapToIdentity(entries));

        return searchResult;
    }

    private void cleanClient() throws LdapException, IOException {
        getClient().unBind();
        getClient().close();
    }

    public class LdapSearchResult {

        private byte[] cookie;
        private List<Identity> identities;

        public byte[] getCookie() {
            return cookie;
        }

        public void setCookie(byte[] cookie) {
            this.cookie = cookie;
        }

        public List<Identity> getIdentities() {
            return identities;
        }

        public void setIdentities(List<Identity> identities) {
            this.identities = identities;
        }
    }

}
