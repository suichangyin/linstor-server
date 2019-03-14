package com.linbit.linstor.api.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.linbit.linstor.Resource;
import com.linbit.linstor.ResourceConnection;
import com.linbit.linstor.ResourceDefinition;
import com.linbit.linstor.Volume;
import com.linbit.linstor.VolumeDefinition;
import com.linbit.linstor.api.interfaces.RscLayerDataApi;

public class RscPojo implements Comparable<RscPojo>, Resource.RscApi
{
    private final String rscName;
    private final String nodeName;
    private final UUID nodeUuid;
    private final ResourceDefinition.RscDfnApi rscDefinition;
    private final UUID localRscUuid;
    private final long localRscFlags;
    private final Map<String, String> localRscProps;
    private final List<Volume.VlmApi> localVlms;
    private final List<OtherRscPojo> otherRscs;
    private final List<ResourceConnection.RscConnApi> rscConnections;
    private final Long fullSyncId;
    private final Long updateId;
    private final RscLayerDataApi rscLayerDataPojo;

    public RscPojo(
        final String rscNameRef,
        final String nodeNameRef,
        final UUID nodeUuidRef,
        final ResourceDefinition.RscDfnApi rscDefinitionRef,
        final UUID localRscUuidRef,
        final long localRscFlagsRef,
        final Map<String, String> localRscPropsRef,
        final List<Volume.VlmApi> localVlmsRef,
        final List<OtherRscPojo> otherRscListRef,
        final List<ResourceConnection.RscConnApi> rscConnectionsRef,
        final Long fullSyncIdRef,
        final Long updateIdRef,
        final RscLayerDataApi rscLayerDataPojoRef
    )
    {
        rscName = rscNameRef;
        nodeName = nodeNameRef;
        nodeUuid = nodeUuidRef;
        rscDefinition = rscDefinitionRef;
        localRscUuid = localRscUuidRef;
        localRscFlags = localRscFlagsRef;
        localRscProps = localRscPropsRef;
        rscConnections = rscConnectionsRef;
        localVlms = localVlmsRef;
        otherRscs = otherRscListRef;
        fullSyncId = fullSyncIdRef;
        updateId = updateIdRef;
        rscLayerDataPojo = rscLayerDataPojoRef;
    }

    /**
     * Simple constructor only initializing must have fields for resource creation.
     *
     * @param rscNameRef Resource name.
     * @param nodeNameRef Node name.
     * @param flagsRef Flags mask value.
     * @param propsRef Property map.
     */
    public RscPojo(
        final String rscNameRef,
        final String nodeNameRef,
        final long flagsRef,
        final Map<String, String> propsRef
    )
    {
        rscName = rscNameRef;
        nodeName = nodeNameRef;
        localRscFlags = flagsRef;
        localRscProps = propsRef;


        nodeUuid = null;
        rscDefinition = null;
        localRscUuid = null;
        localVlms = new ArrayList<>();
        otherRscs = null;
        rscConnections = null;
        fullSyncId = null;
        updateId = null;
        rscLayerDataPojo = null;
    }

    @Override
    public String getName()
    {
        return rscName;
    }

    @Override
    public UUID getUuid()
    {
        return localRscUuid;
    }

    @Override
    public String getNodeName()
    {
        return nodeName;
    }

    @Override
    public UUID getNodeUuid()
    {
        return nodeUuid;
    }

    @Override
    public UUID getRscDfnUuid()
    {
        return rscDefinition.getUuid();
    }

    public long getRscDfnFlags()
    {
        return rscDefinition.getFlags();
    }

    public Map<String, String> getRscDfnProps()
    {
        return rscDefinition.getProps();
    }

    public UUID getLocalRscUuid()
    {
        return localRscUuid;
    }

    public long getLocalRscFlags()
    {
        return localRscFlags;
    }

    @Override
    public long getFlags()
    {
        return localRscFlags;
    }

    public Map<String, String> getLocalRscProps()
    {
        return localRscProps;
    }

    public List<VolumeDefinition.VlmDfnApi> getVlmDfns()
    {
        return rscDefinition.getVlmDfnList();
    }

    public List<Volume.VlmApi> getLocalVlms()
    {
        return localVlms;
    }

    @Override
    public Map<String, String> getProps()
    {
        return localRscProps;
    }

    @Override
    public List<? extends Volume.VlmApi> getVlmList()
    {
        return localVlms;
    }

    public List<OtherRscPojo> getOtherRscList()
    {
        return otherRscs;
    }

    public List<ResourceConnection.RscConnApi> getRscConnections()
    {
        return rscConnections;
    }

    public long getFullSyncId()
    {
        return fullSyncId;
    }

    public long getUpdateId()
    {
        return updateId;
    }

    @Override
    public RscLayerDataApi getLayerData()
    {
        return rscLayerDataPojo;
    }

    @Override
    public int compareTo(RscPojo otherRscPojo)
    {
        return rscName.compareTo(otherRscPojo.rscName);
    }

    public static class OtherRscPojo
    {
        private final String nodeName;
        private final UUID nodeUuid;
        private final String nodeType;
        private final long nodeFlags;
        private final Map<String, String> nodeProps;
        private final UUID rscUuid;
        private final long rscFlags;
        private final Map<String, String> rscProps;
        private final List<Volume.VlmApi> vlms;
        private final List<OtherNodeNetInterfacePojo> netInterfacefPojos;
        private final RscLayerDataApi rscLayerDataPojo;

        public OtherRscPojo(
            String nodeNameRef,
            UUID nodeUuidRef,
            String nodeTypeRef,
            long nodeFlagsRef,
            Map<String, String> nodePropsRef,
            List<OtherNodeNetInterfacePojo> netIfPojosRef,
            UUID rscUuidRef,
            long rscFlagsRef,
            Map<String, String> rscPropsRef,
            List<Volume.VlmApi> vlmsRef,
            RscLayerDataApi rscLayerDataPojoRef
        )
        {
            nodeName = nodeNameRef;
            nodeUuid = nodeUuidRef;
            nodeType = nodeTypeRef;
            nodeFlags = nodeFlagsRef;
            nodeProps = nodePropsRef;
            netInterfacefPojos = netIfPojosRef;
            rscUuid = rscUuidRef;
            rscFlags = rscFlagsRef;
            rscProps = rscPropsRef;
            vlms = vlmsRef;
            rscLayerDataPojo = rscLayerDataPojoRef;
        }

        public String getNodeName()
        {
            return nodeName;
        }

        public UUID getNodeUuid()
        {
            return nodeUuid;
        }

        public String getNodeType()
        {
            return nodeType;
        }

        public long getNodeFlags()
        {
            return nodeFlags;
        }

        public List<OtherNodeNetInterfacePojo> getNetInterfacefPojos()
        {
            return netInterfacefPojos;
        }

        public Map<String, String> getNodeProps()
        {
            return nodeProps;
        }

        public UUID getRscUuid()
        {
            return rscUuid;
        }

        public long getRscFlags()
        {
            return rscFlags;
        }

        public Map<String, String> getRscProps()
        {
            return rscProps;
        }

        public List<Volume.VlmApi> getVlms()
        {
            return vlms;
        }

        public RscLayerDataApi getRscLayerDataPojo()
        {
            return rscLayerDataPojo;
        }
    }

    public static class OtherNodeNetInterfacePojo
    {
        private final UUID uuid;
        private final String name;
        private final String address;

        public OtherNodeNetInterfacePojo(final UUID uuidRef, final String nameRef, final String addressRef)
        {
            uuid = uuidRef;
            name = nameRef;
            address = addressRef;
        }

        public UUID getUuid()
        {
            return uuid;
        }

        public String getName()
        {
            return name;
        }

        public String getAddress()
        {
            return address;
        }
    }

}
