package com.linbit.linstor.transaction;

import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.dbdrivers.DatabaseTable;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorCrd;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorSpec;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorVersionCrd;
import com.linbit.linstor.dbdrivers.k8s.crd.LinstorVersionSpec;
import com.linbit.linstor.dbdrivers.k8s.crd.RollbackCrd;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class K8sCrdTransaction
{
    private final Map<DatabaseTable, MixedOperation<?, ?, ?>> crdClientLut;
    private final MixedOperation<RollbackCrd, KubernetesResourceList<RollbackCrd>, Resource<RollbackCrd>> rollbackClient;
    private final MixedOperation<LinstorVersionCrd, KubernetesResourceList<LinstorVersionCrd>, Resource<LinstorVersionCrd>> linstorVersionClient;

    final HashMap<DatabaseTable, HashMap<String, LinstorCrd<?>>> rscsToChangeOrCreate;
    final HashMap<DatabaseTable, HashMap<String, LinstorCrd<?>>> rscsToDelete;

    public K8sCrdTransaction(
        Map<DatabaseTable, MixedOperation<?, ?, ?>> crdClientLutRef,
        MixedOperation<RollbackCrd, KubernetesResourceList<RollbackCrd>, Resource<RollbackCrd>> rollbackClientRef,
        MixedOperation<LinstorVersionCrd, KubernetesResourceList<LinstorVersionCrd>, Resource<LinstorVersionCrd>> linstorVersionClientRef
    )
    {
        crdClientLut = crdClientLutRef;
        rollbackClient = rollbackClientRef;
        linstorVersionClient = linstorVersionClientRef;

        rscsToChangeOrCreate = new HashMap<>();
        rscsToDelete = new HashMap<>();
    }

    public MixedOperation<RollbackCrd, KubernetesResourceList<RollbackCrd>, Resource<RollbackCrd>> getRollbackClient()
    {
        return rollbackClient;
    }

    public MixedOperation<LinstorVersionCrd, KubernetesResourceList<LinstorVersionCrd>, Resource<LinstorVersionCrd>> getLinstorVersionClient()
    {
        return linstorVersionClient;
    }

    public void updateLinstorVersion(int version)
    {
        LinstorVersionCrd linstorVersion = new LinstorVersionCrd(new LinstorVersionSpec(version));
        linstorVersionClient.createOrReplace(linstorVersion);
    }

    public static boolean hasClientValidCrd(MixedOperation<?, ?, ?> client)
    {
        boolean ret;
        try
        {
            client.list();
            ret = true;
        }
        catch (KubernetesClientException exc)
        {
            ret = false;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec> MixedOperation<CRD, KubernetesResourceList<CRD>, Resource<CRD>> getClient(
        DatabaseTable dbTable
    )
    {
        return (MixedOperation<CRD, KubernetesResourceList<CRD>, Resource<CRD>>) crdClientLut
            .get(dbTable);
    }

    public void update(DatabaseTable dbTable, LinstorCrd<?> k8sRsc)
    {
        // System.out.println("updating " + dbTable.getName() + " " + k8sRsc);
        lazyGet(rscsToChangeOrCreate, dbTable).put(k8sRsc.getKey(), k8sRsc);
    }

    public void delete(DatabaseTable dbTable, LinstorCrd<?> k8sRsc)
    {
        // System.out.println("deleting entry from " + dbTable.getName() + ": " + k8sRsc);
        lazyGet(rscsToDelete, dbTable).put(k8sRsc.getKey(), k8sRsc);
    }

    private HashMap<String, LinstorCrd<?>> lazyGet(
        HashMap<DatabaseTable, HashMap<String, LinstorCrd<?>>> map,
        DatabaseTable dbTableRef
    )
    {
        HashMap<String, LinstorCrd<?>> ret = map.get(dbTableRef);
        if (ret == null)
        {
            ret = new HashMap<>();
            map.put(dbTableRef, ret);
        }
        return ret;
    }

    public <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec> HashMap<String, SPEC> get(DatabaseTable dbTable)
    {
        return this.<CRD, SPEC>get(dbTable, ignored -> true);
    }

    public <CRD extends LinstorCrd<SPEC>, SPEC extends LinstorSpec> HashMap<String, SPEC> get(
        DatabaseTable dbTable,
        Predicate<SPEC> matcher
    )
    {
        HashMap<String, SPEC> ret = new HashMap<>();
        MixedOperation<CRD, KubernetesResourceList<CRD>, Resource<CRD>> client = getClient(
            dbTable
        );
        KubernetesResourceList<CRD> list = client.list();
        for (CRD item : list.getItems())
        {
            SPEC spec = item.getSpec();
            if (matcher.test(spec))
            {
                ret.put(spec.getKey(), spec);
            }
        }
        return ret;
    }

    public <SPEC extends LinstorSpec> SPEC get(
        DatabaseTable dbTable,
        Predicate<SPEC> matcher,
        boolean failIfNullRef,
        String notFoundMessage
    )
        throws DatabaseException
    {
        SPEC ret = null;
        HashMap<String, SPEC> map = get(dbTable, matcher);
        if (map.isEmpty())
        {
            if (failIfNullRef)
            {
                throw new DatabaseException(notFoundMessage);
            }
        }
        else if (map.size() > 1)
        {
            throw new DatabaseException("Duplicate entry for single get");
        }
        else
        {
            ret = map.values().iterator().next();
        }
        return ret;
    }
}