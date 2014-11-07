package com.github.vlsi.mat.optiq;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.*;

public class HeapFunctions {
    public static int get_id(Object r) {
        HeapReference ref = ensureHeapReference(r);
        return ref == null ? -1 : ref.getIObject().getObjectId();
    }

    public static String toString(Object r) {
        if (r == null) return null;
        return r.toString();
    }

    public static Object get_by_key(Object r, String value) {
        HeapReference ref = ensureHeapReference(r);
        if (ref == null) {
            return null;
        }
        IObject iObject = ref.getIObject();
        ISnapshot snapshot = iObject.getSnapshot();
        String className = iObject.getClazz().getName();

        if (!"java.util.HashMap".equals(className))
        {
            throw new RuntimeException("Unsupported map type: "+className);
        }

        try
        {
            IObjectArray table = (IObjectArray)iObject.resolveValue("table");
            long[] referenceArray = table.getReferenceArray();
            for (long entryAddress : referenceArray)
            {
                if (entryAddress != 0)
                {
                    int entryId = snapshot.mapAddressToId(entryAddress);
                    IObject entry = snapshot.getObject(entryId);
                    while (entry != null)
                    {
                        IObject keyObject = (IObject) entry.resolveValue("key");
                        if (value.equals(toString(keyObject)))
                        {
                            IObject valueObject = (IObject) entry.resolveValue(("value"));
                            if (valueObject != null)
                            {
                                return new HeapReference(snapshot, valueObject);
                            }
                            else
                            {
                                return null;
                            }
                        }

                        entry = (IObject)entry.resolveValue("next");
                    }
                }
            }
        } catch (SnapshotException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static HeapReference ensureHeapReference(Object r) {
        return (r == null || !(r instanceof HeapReference)) ?
                null :
                (HeapReference) r;
    }

    private static String toString(IObject o) {
        String classSpecific = o.getClassSpecificName();
        if (classSpecific != null)
            return classSpecific;
        return o.getDisplayName();
    }
}