# nextcloud
Online document space management service for ENT synchronized on different devices using Nextcloud service

# About
* Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt)
* Developer : CGI
* Description : Online document space management service for ENT synchronized on different devices using Nextcloud service.

## Configuration

Specific configuration that must be seen : 
<pre>
{
      ...
      "config": {
        ...
        "admin-credential" : {
            "username": "${adminNextcloudUsername}",
            "password": "${adminNextcloudPassword}"
        },
        "nextcloud-host": "${nextcloudHost}",
        "endpoint" : {
            "ocs-api": "${nextcloudOCSAPI}",
            "webdav-api": "${nextcloudWebdavAPI}"
        }
      }
}
</pre>

In your springboard, you must include these variables :

<pre>
adminNextcloudUsername=${String}
adminNextcloudPassword=${String}
nextcloudHost=${String}
nextcloudOCSAPI=${String}
nextcloudWebdavAPI=${String}
</pre>

# Documentation API
* [OCS](https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html)
* [Webdav](https://docs.nextcloud.com/server/latest/developer_manual/client_apis/WebDAV/basic.html#)
