package com.linbit.linstor.core.objects;

import com.linbit.linstor.LinStorDataAlreadyExistsException;
import com.linbit.linstor.core.types.NodeId;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.dbdrivers.interfaces.SnapshotDatabaseDriver;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.stateflags.StateFlagsBits;
import com.linbit.linstor.storage.kinds.DeviceLayerKind;
import com.linbit.linstor.transaction.TransactionMgr;
import com.linbit.linstor.transaction.TransactionObjectFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Singleton
public class SnapshotControllerFactory
{
    private final SnapshotDatabaseDriver driver;
    private final TransactionObjectFactory transObjFactory;
    private final Provider<TransactionMgr> transMgrProvider;

    @Inject
    public SnapshotControllerFactory(
        SnapshotDatabaseDriver driverRef,
        TransactionObjectFactory transObjFactoryRef,
        Provider<TransactionMgr> transMgrProviderRef
    )
    {
        driver = driverRef;
        transObjFactory = transObjFactoryRef;
        transMgrProvider = transMgrProviderRef;
    }

    public Snapshot create(
        AccessContext accCtx,
        Node node,
        SnapshotDefinition snapshotDfn,
        NodeId nodeId,
        Snapshot.Flags[] initFlags,
        List<DeviceLayerKind> layerStack
    )
        throws DatabaseException, AccessDeniedException, LinStorDataAlreadyExistsException
    {
        snapshotDfn.getResourceDefinition().getObjProt().requireAccess(accCtx, AccessType.USE);

        Snapshot snapshot = snapshotDfn.getSnapshot(accCtx, node.getName());

        if (snapshot != null)
        {
            throw new LinStorDataAlreadyExistsException("The Snapshot already exists");
        }

        snapshot = new Snapshot(
            UUID.randomUUID(),
            snapshotDfn,
            node,
            nodeId,
            StateFlagsBits.getMask(initFlags),
            driver, transObjFactory, transMgrProvider,
            new TreeMap<>(),
            layerStack
        );

        driver.create(snapshot);
        snapshotDfn.addSnapshot(accCtx, snapshot);
        node.addSnapshot(accCtx, snapshot);

        return snapshot;
    }
}