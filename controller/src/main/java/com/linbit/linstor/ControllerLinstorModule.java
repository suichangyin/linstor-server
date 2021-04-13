package com.linbit.linstor;

import com.linbit.linstor.core.repository.ExternalFileProtectionRepository;
import com.linbit.linstor.core.repository.ExternalFileRepository;
import com.linbit.linstor.core.repository.FreeSpaceMgrProtectionRepository;
import com.linbit.linstor.core.repository.FreeSpaceMgrRepository;
import com.linbit.linstor.core.repository.KeyValueStoreProtectionRepository;
import com.linbit.linstor.core.repository.KeyValueStoreRepository;
import com.linbit.linstor.core.repository.NodeProtectionRepository;
import com.linbit.linstor.core.repository.NodeRepository;
import com.linbit.linstor.core.repository.ResourceDefinitionProtectionRepository;
import com.linbit.linstor.core.repository.ResourceDefinitionRepository;
import com.linbit.linstor.core.repository.ResourceGroupProtectionRepository;
import com.linbit.linstor.core.repository.ResourceGroupRepository;
import com.linbit.linstor.core.repository.StorPoolDefinitionProtectionRepository;
import com.linbit.linstor.core.repository.StorPoolDefinitionRepository;
import com.linbit.linstor.core.repository.SystemConfProtectionRepository;
import com.linbit.linstor.core.repository.SystemConfRepository;

import com.google.inject.AbstractModule;

public class ControllerLinstorModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(NodeRepository.class).to(NodeProtectionRepository.class);
        bind(ResourceDefinitionRepository.class).to(ResourceDefinitionProtectionRepository.class);
        bind(ResourceGroupRepository.class).to(ResourceGroupProtectionRepository.class);
        bind(StorPoolDefinitionRepository.class).to(StorPoolDefinitionProtectionRepository.class);
        bind(FreeSpaceMgrRepository.class).to(FreeSpaceMgrProtectionRepository.class);
        bind(SystemConfRepository.class).to(SystemConfProtectionRepository.class);
        bind(KeyValueStoreRepository.class).to(KeyValueStoreProtectionRepository.class);
        bind(ExternalFileRepository.class).to(ExternalFileProtectionRepository.class);
    }
}
