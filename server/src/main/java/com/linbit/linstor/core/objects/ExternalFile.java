package com.linbit.linstor.core.objects;

import com.linbit.ErrorCheck;
import com.linbit.linstor.AccessToDeletedDataException;
import com.linbit.linstor.DbgInstanceUuid;
import com.linbit.linstor.api.pojo.ExternalFilePojo;
import com.linbit.linstor.core.identifier.ExternalFileName;
import com.linbit.linstor.dbdrivers.DatabaseException;
import com.linbit.linstor.dbdrivers.interfaces.ExternalFileDatabaseDriver;
import com.linbit.linstor.security.AccessContext;
import com.linbit.linstor.security.AccessDeniedException;
import com.linbit.linstor.security.AccessType;
import com.linbit.linstor.security.ObjectProtection;
import com.linbit.linstor.security.ProtectedObject;
import com.linbit.linstor.stateflags.FlagsHelper;
import com.linbit.linstor.stateflags.StateFlags;
import com.linbit.linstor.transaction.BaseTransactionObject;
import com.linbit.linstor.transaction.TransactionObjectFactory;
import com.linbit.linstor.transaction.TransactionSimpleObject;
import com.linbit.linstor.transaction.manager.TransactionMgr;
import com.linbit.linstor.utils.ByteUtils;

import javax.inject.Provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ExternalFile extends BaseTransactionObject
    implements DbgInstanceUuid, Comparable<ExternalFile>, ProtectedObject
{
    // Object identifier
    private final UUID objId;

    // Runtime instance identifier for debug purposes
    private final transient UUID dbgInstanceId;

    // File name (not the path, more like a short descriptive name for linstor)
    private final ExternalFileName fileName;

    // State flags
    private final StateFlags<Flags> flags;

    private final TransactionSimpleObject<ExternalFile, byte[]> content;
    private final TransactionSimpleObject<ExternalFile, byte[]> contentCheckSum;
    private final TransactionSimpleObject<ExternalFile, byte[]> localContentCheckSum; // stlt only

    private final TransactionSimpleObject<ExternalFile, Boolean> deleted;

    private final ObjectProtection objProt;
    private final ExternalFileDatabaseDriver dbDriver;


    ExternalFile(
        UUID uuidRef,
        ObjectProtection objProtRef,
        ExternalFileName extFileNameRef,
        long initFlagsRef,
        byte[] contentRef,
        byte[] contentCheckSumRef,
        ExternalFileDatabaseDriver dbDriverRef,
        TransactionObjectFactory transObjFactory,
        Provider<? extends TransactionMgr> transMgrProviderRef
    )
    {
        super(transMgrProviderRef);
        ErrorCheck.ctorNotNull(ExternalFile.class, ExternalFileName.class, extFileNameRef);
        ErrorCheck.ctorNotNull(ExternalFile.class, byte[].class, contentRef);

        objId = uuidRef;
        dbgInstanceId = UUID.randomUUID();
        objProt = objProtRef;
        fileName = extFileNameRef;
        dbDriver = dbDriverRef;

        content = transObjFactory.createTransactionSimpleObject(this, contentRef, dbDriver.getContentDriver());
        contentCheckSum = transObjFactory.createTransactionSimpleObject(
            this,
            contentCheckSumRef,
            dbDriver.getContentCheckSumDriver()
        );
        localContentCheckSum = transObjFactory.createTransactionSimpleObject(
            this,
            new byte[0],
            null
        );

        deleted = transObjFactory.createTransactionSimpleObject(this, false, null);

        flags = transObjFactory.createStateFlagsImpl(
            objProt,
            this,
            Flags.class,
            dbDriver.getStateFlagPersistence(),
            initFlagsRef
        );

        transObjs = Arrays.asList(content, flags);
    }

    @Override
    public int compareTo(ExternalFile otherFile)
    {
        return fileName.compareTo(otherFile.getName());
    }

    public UUID getUuid()
    {
        checkDeleted();
        return objId;
    }

    @Override
    public UUID debugGetVolatileUuid()
    {
        return dbgInstanceId;
    }

    public ExternalFileName getName()
    {
        checkDeleted();
        return fileName;
    }

    public StateFlags<Flags> getFlags()
    {
        checkDeleted();
        return flags;
    }

    public byte[] getContentCheckSum(AccessContext accCtx) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return contentCheckSum.get();
    }

    public String getContentCheckSumHex(AccessContext accCtx) throws AccessDeniedException
    {
        return ByteUtils.bytesToHex(getContentCheckSum(accCtx));
    }

    /*
     * used by satellite only -> no access check
     */
    public void setLocalContentCheckSum(byte[] chkSum) throws DatabaseException
    {
        checkDeleted();
        localContentCheckSum.set(chkSum);
    }

    /*
     * used by satellite only -> no access check
     */
    public boolean needsRewriteChanged(AccessContext wrkCtxRef)
    {
        checkDeleted();
        return !Arrays.equals(contentCheckSum.get(), localContentCheckSum.get());
    }

    public byte[] getContent(AccessContext accCtx) throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return content.get();
    }

    public void setContent(AccessContext accCtx, byte[] contentRef) throws DatabaseException, AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.CHANGE);
        byte[] checksum = ByteUtils.checksumSha256(contentRef);
        content.set(contentRef);
        contentCheckSum.set(checksum);
    }

    @Override
    public ObjectProtection getObjProt()
    {
        checkDeleted();
        return objProt;
    }

    public ExternalFilePojo getApiData(
        AccessContext accCtx,
        Long fullSyncId,
        Long updateId
    )
        throws AccessDeniedException
    {
        checkDeleted();
        objProt.requireAccess(accCtx, AccessType.VIEW);
        return new ExternalFilePojo(
            objId,
            fileName.extFileName,
            flags.getFlagsBits(accCtx),
            content.get(),
            contentCheckSum.get(),
            fullSyncId,
            updateId
        );
    }

    public boolean isDeleted()
    {
        return deleted.get();
    }

    private void checkDeleted()
    {
        if (deleted.get())
        {
            throw new AccessToDeletedDataException("Access to deleted object");
        }
    }

    public void delete(AccessContext accCtx) throws AccessDeniedException, DatabaseException
    {
        if (!deleted.get())
        {
            objProt.requireAccess(accCtx, AccessType.CONTROL);

            objProt.delete(accCtx);

            activateTransMgr();
            dbDriver.delete(this);

            deleted.set(true);
        }
    }

    public interface InitMaps
    {
        // empty for now
    }

    public enum Flags implements com.linbit.linstor.stateflags.Flags
    {
        DELETE(1L);

        public long flagValue;

        Flags(long valueRef)
        {
            flagValue = valueRef;
        }

        @Override
        public long getFlagValue()
        {
            return flagValue;
        }

        public static Flags[] valuesOfIgnoreCase(String string)
        {
            Flags[] flags;
            if (string == null)
            {
                flags = new Flags[0];
            }
            else
            {
                String[] split = string.split(",");
                flags = new Flags[split.length];

                for (int idx = 0; idx < split.length; idx++)
                {
                    flags[idx] = Flags.valueOf(split[idx].toUpperCase().trim());
                }
            }
            return flags;
        }

        public static Flags[] restoreFlags(long extFileFlags)
        {
            List<Flags> flagList = new ArrayList<>();
            for (Flags flag : Flags.values())
            {
                if ((extFileFlags & flag.flagValue) == flag.flagValue)
                {
                    flagList.add(flag);
                }
            }
            return flagList.toArray(new Flags[flagList.size()]);
        }

        public static List<String> toStringList(long flagsMask)
        {
            return FlagsHelper.toStringList(Flags.class, flagsMask);
        }

        public static long fromStringList(List<String> listFlags)
        {
            return FlagsHelper.fromStringList(Flags.class, listFlags);
        }
    }
}
