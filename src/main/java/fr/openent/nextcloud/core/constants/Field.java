package fr.openent.nextcloud.core.constants;

public class Field {

    public static final String DATA = "data";
    public static final String ID = "id";
    public static final String USERID = "userid";
    public static final String PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String DISPLAYNAME = "name";
    public static final String EMAIL = "email";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "address";
    public static final String USED = "used";
    public static final String QUOTA = "quota";

    public static final String STATUS = "status";
    public static final String STATUSCODE = "statuscode";
    public static final String MESSAGE = "message";
    public static final String TOTALITEMS = "totalitems";
    public static final String ITEMSPERPAGE = "itemsperpage";
    public static final String META = "meta";
    public static final String PATH = "path";
    public static final String OCS = "ocs";

    public static final String ISFOLDER = "isFolder";

    // Config
    public static final String ADMINCREDENTIAL = "admin-credential";
    public static final String ENDPOINT = "endpoint";
    public static final String OCS_ENDPOINT_API = "ocs-api";
    public static final String WEBDAV_ENDPOINT_API = "webdav-api";
    public static final String NEXTCLOUDHOST = "nextcloud-host";
    public static final String OCS_API_REQUEST = "OCS-APIRequest";

    // ProxyConf
    public static final String HTTP_CLIENT_PROXY_HOST = "httpclient.proxyHost";
    public static final String HTTP_CLIENT_PROXY_PORT = "httpclient.proxyPort";
    public static final String HTTP_CLIENT_PROXY_USERNAME = "httpclient.proxyUsername";
    public static final String HTTP_CLIENT_PROXY_PASSWORD = "httpclient.proxyPassword";

    // propfind param
    public static final String D_MULTISTATUS = "d:multistatus";
    public static final String D_RESPONSE = "d:response";
    public static final String D_PROPFIND = "d:propfind";
    public static final String D_PROPSTAT = "d:propstat";
    public static final String D_PROP = "d:prop";
    public static final String D_HREF = "d:href";
    public static final String D_GETLASTMODIFIED = "d:getlastmodified";
    public static final String D_GETETAG = "d:getetag";
    public static final String D_GETCONTENTTYPE = "d:getcontenttype";
    public static final String D_RESOURCETYPE = "d:resourcetype";
    public static final String OC_FILEID = "oc:fileid";
    public static final String OC_PERMISSIONS = "oc:permissions";
    public static final String OC_SIZE = "oc:size";
    public static final String D_GETCONTENTLENGTH = "d:getcontentlength";
    public static final String NC_HAS_PREVIEW = "nc:has-preview";
    public static final String OC_FAVORITE = "oc:favorite";
    public static final String OC_COMMENTS_UNREAD = "oc:comments-unread";
    public static final String OC_OWNER_DISPLAY_NAME = "oc:owner-display-name";
    public static final String OC_SHARE_TYPE = "oc:share-types";
    
    // propfind json format 
    public static final String LASTMODIFIED = "lastModified";
    public static final String ETAG = "etag";
    public static final String CONTENTTYPE = "contentType";
    public static final String RESOURCETYPE = "resourceType";
    public static final String FILEID = "fileId";
    public static final String PERMISSIONS = "permissions";
    public static final String SIZE = "size";
    public static final String CONTENTLENGTH = "contentLength";
    public static final String HASPREVIEW = "hasPreview";
    public static final String FAVORITE = "favorite";
    public static final String COMMENTSUNREAD = "commentsUnread";
    public static final String OWNERDISPLAYNAME = "ownerDisplayName";
    public static final String SHARETYPE = "shareTypes";
    



    private Field() {
        throw new IllegalStateException("Utility class");
    }
}
