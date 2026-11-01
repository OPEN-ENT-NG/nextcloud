# nextcloud

Online document space management service for ENT synchronized on different devices using Nextcloud service

# About

- Licence : [AGPL v3](http://www.gnu.org/licenses/agpl.txt) - Copyright Région Normandie, CGI
- Financer : Région Normandie, CGI
- Developer : CGI, Edifice
- Description : Online document space management service for ENT synchronized on different devices using Nextcloud service.

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
        },
        "quota": "${nextcloudQuota}",
        "is-nextcloud-url-hidden": ${isNextcloudUrlHidden}
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
nextcloudQuota=${String}
isNextcloudUrlHidden=${Boolean} # true if you want to hide the nextcloud url on workspace

# ENDPOINT
nextcloudOCSAPI=/ocs/v1.php
nextcloudWebdavAPI=/remote.php/dav/files

# Misc
nextcloudQuota=2 GB
</pre>

## Console Nextcloud (desktop)

The route to retrieve the Nextcloud desktop client configuration (within the intranet) is protected by a workflow permission. To access it from outside, and especially from the client, a connector must be created in the intranet:

- URL: `https://plateforme.fr/nextcloud/desktop/config`
- clientId: `nextcloudDesktopAdmin`
- scope: `fr.openent.nextcloud.controller.NextcloudDesktopController|getConfig`
- secret code: `<password>`

Once the connector is created, you should be able to test with the following curl request:

```
curl -u nextcloudDesktopAdmin:\<password> -X GET https://plateforme.fr/nextcloud/desktop/config
```

## Usage (workspace module only)

Call sniplet workspace-nextcloud-tree where nextcloud front logical
will be called to use synchronized documents :

```
<sniplet template="nextcloud-folder/workspace-nextcloud-folder" application="nextcloud"></sniplet>
```

You also need to add **_"enable-nextcloud": true_** in ent-core.json, under workspace service :

<pre>
[workspace config]
{
    ...
    "name": "org.entcore~workspace~...",
    "config": {
        ...
        "enable-nextcloud": true,
        ...
    }
    ...
}
</pre>

## Event bus API (other ENT modules)

Address `fr.openent.nextcloud`, action `copyFromStorage`: copy files of the ENT storage into the Nextcloud of a user.
The files stay in the ENT storage. **The calling module must check that the user can access these files**
(Nextcloud cannot know the rights of another module's resources) and that the user has the Nextcloud `view` right.

```json
{
  "action": "copyFromStorage",
  "userId": "<ENT user id, owner of the destination Nextcloud>",
  "host": "<request host, key of the Nextcloud configuration>",
  "path": "/Documents/Casier/",
  "files": [
    { "fileId": "<ENT storage file id>", "metadata": { "filename": "report.pdf", "content-type": "application/pdf", "size": 1024 } }
  ]
}
```

- `path` is optional (Nextcloud root by default). A file whose name already exists is renamed `name (1).ext`.
- Reply: `{ "status": "ok", "data": [{ "fileId", "name", "status": "ok" | "ko", "error"? }] }`, one entry per file.
- Failures: `400` invalid message, `401` `nextcloud.session.missing` when the user has never connected to Nextcloud
  (the front must offer the OAuth2 login), `500` otherwise.
- Files are uploaded one after the other: use a send timeout longer than the default 30s for large selections.

Example: Rack, `POST /rack/copy/nextcloud`.

# Documentation API

- [OCS](https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html)
- [Webdav](https://docs.nextcloud.com/server/latest/developer_manual/client_apis/WebDAV/basic.html#)

# SSO

### Installation

- Must use `SSO & SAML Authentication` plugin on Nextcloud [Guidelines](https://apps.nextcloud.com/apps/user_saml)
- Must inject trigger SQL (see SQL script part) in order to propagate user saml within oc_user

## SQL trigger injection

The reason we have to use trigger injection is because nextcloud has 2 distinct table : "oc_users" and "oc_user_saml_users"

- "oc_users" occurs when you generate a user from nextcloud
- "oc_user_saml_users" occurs when you generate a user with nextcloud's SSO

If you log into the application via SSO FIRSTLY, you will generate a user in user_saml.
Problem is, ENT through nextcloud can only read "oc_users" within webdav protocol
To resolve this issues, we have to "duplicate" the user data oc_users and user_saml

**You need to use "_nextcloud_" in order to access and interact tables in database**

### postgresql

```sql
# propagate user (duplicate user saml into oc_user)

CREATE OR REPLACE FUNCTION propagate_user() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT into oc_users(uid, displayname, uid_lower)
    VALUES (NEW.uid, NEW.displayname, NEW.uid);
    RETURN NEW;
END
$BODY$
    LANGUAGE plpgsql;

CREATE TRIGGER propagate_user
    AFTER INSERT
    ON oc_user_saml_users
    FOR EACH ROW
EXECUTE PROCEDURE propagate_user();


# delete propagate user (if user saml deleted, then the duplicated user "oc_users" shall be deleted too)

CREATE OR REPLACE FUNCTION delete_propagate_user() RETURNS TRIGGER AS
$BODY$
BEGIN
    DELETE FROM oc_users WHERE uid = OLD.uid;
    RETURN OLD;
END
$BODY$
    LANGUAGE plpgsql;

CREATE TRIGGER delete_propagate_user
    AFTER DELETE
    ON oc_user_saml_users
    FOR EACH ROW
EXECUTE PROCEDURE delete_propagate_user();

```

Check its creation (trigger and function)

```postgresql
# trigger
SELECT * FROM information_schema.triggers WHERE trigger_name = 'propagate_user';

# function
SELECT routine_name FROM information_schema.routines WHERE routine_type = 'FUNCTION' AND routine_name = 'propagate_user';

# also workss for delete_propagate_user trigger/function
```

Drop its triggers/functions

```postgresql
# trigger
DROP TRIGGER propagate_user ON oc_user_saml_users;

# function
DROP function propagate_user(); # must delete trigger first since it depends on it

# also works for delete_propagate_user trigger/function
```

You can check the document for accessing your database :

- [Database configuration](https://docs.nextcloud.com/server/latest/admin_manual/configuration_database/linux_database_configuration.html)
