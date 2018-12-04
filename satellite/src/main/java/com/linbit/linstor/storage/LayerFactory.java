package com.linbit.linstor.storage;

import com.linbit.linstor.storage.layer.ResourceLayer;
import com.linbit.linstor.storage.layer.adapter.DefaultLayer;
import com.linbit.linstor.storage.layer.adapter.drbd.DrbdLayer;
import com.linbit.linstor.storage.layer.provider.StorageLayer;
import com.linbit.linstor.storage2.layer.kinds.DefaultLayerKind;
import com.linbit.linstor.storage2.layer.kinds.DeviceLayerKind;
import com.linbit.linstor.storage2.layer.kinds.DrbdLayerKind;
import com.linbit.linstor.storage2.layer.kinds.StorageLayerKind;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class LayerFactory
{
    private final Map<Class<? extends DeviceLayerKind>, ResourceLayer> devLayerLookupTable;

    @Inject
    public LayerFactory(
        DefaultLayer dfltLayer,
        DrbdLayer drbdLayer,
        StorageLayer storageLayer
    )
    {
        devLayerLookupTable = new HashMap<>();

        devLayerLookupTable.put(DefaultLayerKind.class, dfltLayer);
        devLayerLookupTable.put(DrbdLayerKind.class, drbdLayer);
        devLayerLookupTable.put(StorageLayerKind.class, storageLayer);
    }

    public ResourceLayer getDeviceLayer(Class<? extends DeviceLayerKind> kindClass)
    {
        return devLayerLookupTable.get(kindClass);
    }
}
