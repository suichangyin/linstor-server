package com.linbit.drbdmanage;

import com.linbit.drbdmanage.security.ObjectProtection;
import java.util.UUID;

/**
 * Definition of a storage pool
 *
 * @author Robert Altnoeder &lt;robert.altnoeder@linbit.com&gt;
 */
public interface StorPoolDefinition
{
    public UUID getUuid();

    public ObjectProtection getObjProt();

    public StorPoolName getName();
}
