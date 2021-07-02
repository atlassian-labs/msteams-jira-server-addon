package com.microsoft.teams.ao;

import net.java.ao.Entity;
import net.java.ao.schema.StringLength;

public interface AppKeys extends Entity {

    String getConsumerKey();

    void setConsumerKey(String consumerKey);

    @StringLength(StringLength.UNLIMITED)
    String getPrivateKey();

    @StringLength(StringLength.UNLIMITED)
    void setPrivateKey(String privateKey);

    @StringLength(StringLength.UNLIMITED)
    String getPublicKey();

    @StringLength(StringLength.UNLIMITED)
    void setPublicKey(String publicKey);

    String getAtlasId();

    void setAtlasId(String atlasId);
}
