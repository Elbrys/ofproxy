package com.elbrys.sdn.ofproxy.openflow.protocol;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

/**
 * Class that builds
 * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OF10FeaturesRequestMessage}
 * instances.
 * 
 * @see org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OF10FeaturesRequestMessage
 * 
 */
public final class OF10FeaturesRequestMessageBuilder implements Builder<OF10FeaturesRequestMessage> {

    private java.lang.Short _version;
    private static List<Range<BigInteger>> _version_range;
    private java.lang.Long _xid;
    private static List<Range<BigInteger>> _xid_range;

    Map<java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>>, Augmentation<OF10FeaturesRequestMessage>> augmentation = new HashMap<>();

    public OF10FeaturesRequestMessageBuilder() {
    }

    public OF10FeaturesRequestMessageBuilder(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesRequest arg) {
        this._version = arg.getVersion();
        this._xid = arg.getXid();
    }

    public OF10FeaturesRequestMessageBuilder(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader arg) {
        this._version = arg.getVersion();
        this._xid = arg.getXid();
    }

    public OF10FeaturesRequestMessageBuilder(final OF10FeaturesRequestMessage base) {
        this._version = base.getVersion();
        this._xid = base.getXid();
        if (base instanceof FeaturesRequestMessageImpl) {
            FeaturesRequestMessageImpl impl = (FeaturesRequestMessageImpl) base;
            this.augmentation = new HashMap<>(impl.augmentation);
        }
    }

    /**
     * Set fields from given grouping argument. Valid argument is instance of
     * one of following types:
     * <ul>
     * <li>org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.
     * rev130731.Hello</li>
     * <li>org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.
     * rev130731.OfHeader</li>
     * </ul>
     * 
     * @param arg
     *            grouping object
     * @throws IllegalArgumentException
     *             if given argument is none of valid types
     */
    public void fieldsFrom(DataObject arg) {
        boolean isValidArg = false;
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesRequest) {
            isValidArg = true;
        }
        if (arg instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader) {
            this._version = ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader) arg)
                    .getVersion();
            this._xid = ((org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader) arg)
                    .getXid();
            isValidArg = true;
        }
        if (!isValidArg) {
            throw new IllegalArgumentException(
                    "expected one of: [org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.Hello, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader] \n"
                            + "but was: " + arg);
        }
    }

    public java.lang.Short getVersion() {
        return _version;
    }

    public java.lang.Long getXid() {
        return _xid;
    }

    @SuppressWarnings("unchecked")
    public <E extends Augmentation<OF10FeaturesRequestMessage>> E getAugmentation(java.lang.Class<E> augmentationType) {
        if (augmentationType == null) {
            throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
        }
        return (E) augmentation.get(augmentationType);
    }

    public OF10FeaturesRequestMessageBuilder setVersion(java.lang.Short value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value);
            boolean isValidRange = false;
            for (Range<BigInteger> r : _version_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value,
                        _version_range));
            }
        }
        this._version = value;
        return this;
    }

    public static List<Range<BigInteger>> _version_range() {
        if (_version_range == null) {
            synchronized (OF10FeaturesRequestMessageBuilder.class) {
                if (_version_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(255L)));
                    _version_range = builder.build();
                }
            }
        }
        return _version_range;
    }

    public OF10FeaturesRequestMessageBuilder setXid(java.lang.Long value) {
        if (value != null) {
            BigInteger _constraint = BigInteger.valueOf(value);
            boolean isValidRange = false;
            for (Range<BigInteger> r : _xid_range()) {
                if (r.contains(_constraint)) {
                    isValidRange = true;
                }
            }
            if (!isValidRange) {
                throw new IllegalArgumentException(String.format("Invalid range: %s, expected: %s.", value, _xid_range));
            }
        }
        this._xid = value;
        return this;
    }

    public static List<Range<BigInteger>> _xid_range() {
        if (_xid_range == null) {
            synchronized (OF10FeaturesRequestMessageBuilder.class) {
                if (_xid_range == null) {
                    ImmutableList.Builder<Range<BigInteger>> builder = ImmutableList.builder();
                    builder.add(Range.closed(BigInteger.ZERO, BigInteger.valueOf(4294967295L)));
                    _xid_range = builder.build();
                }
            }
        }
        return _xid_range;
    }

    public OF10FeaturesRequestMessageBuilder addAugmentation(
            java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>> augmentationType,
            Augmentation<OF10FeaturesRequestMessage> augmentation) {
        if (augmentation == null) {
            return removeAugmentation(augmentationType);
        }
        this.augmentation.put(augmentationType, augmentation);
        return this;
    }

    public OF10FeaturesRequestMessageBuilder removeAugmentation(
            java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>> augmentationType) {
        this.augmentation.remove(augmentationType);
        return this;
    }

    public OF10FeaturesRequestMessage build() {
        return new FeaturesRequestMessageImpl(this);
    }

    private static final class FeaturesRequestMessageImpl implements OF10FeaturesRequestMessage {

        public java.lang.Class<OF10FeaturesRequestMessage> getImplementedInterface() {
            return OF10FeaturesRequestMessage.class;
        }

        private final java.lang.Short _version;
        private final java.lang.Long _xid;

        private Map<java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>>, Augmentation<OF10FeaturesRequestMessage>> augmentation = new HashMap<>();

        private FeaturesRequestMessageImpl(OF10FeaturesRequestMessageBuilder base) {
            this._version = base.getVersion();
            this._xid = base.getXid();
            switch (base.augmentation.size()) {
            case 0:
                this.augmentation = Collections.emptyMap();
                break;
            case 1:
                final Map.Entry<java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>>, Augmentation<OF10FeaturesRequestMessage>> e = base.augmentation
                        .entrySet().iterator().next();
                this.augmentation = Collections
                        .<java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>>, Augmentation<OF10FeaturesRequestMessage>> singletonMap(
                                e.getKey(), e.getValue());
                break;
            default:
                this.augmentation = new HashMap<>(base.augmentation);
            }
        }

        @Override
        public java.lang.Short getVersion() {
            return _version;
        }

        @Override
        public java.lang.Long getXid() {
            return _xid;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <E extends Augmentation<OF10FeaturesRequestMessage>> E getAugmentation(
                java.lang.Class<E> augmentationType) {
            if (augmentationType == null) {
                throw new IllegalArgumentException("Augmentation Type reference cannot be NULL!");
            }
            return (E) augmentation.get(augmentationType);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_version == null) ? 0 : _version.hashCode());
            result = prime * result + ((_xid == null) ? 0 : _xid.hashCode());
            result = prime * result + ((augmentation == null) ? 0 : augmentation.hashCode());
            return result;
        }

        @Override
        public boolean equals(java.lang.Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof DataObject)) {
                return false;
            }
            if (!OF10FeaturesRequestMessage.class.equals(((DataObject) obj).getImplementedInterface())) {
                return false;
            }
            OF10FeaturesRequestMessage other = (OF10FeaturesRequestMessage) obj;
            if (_version == null) {
                if (other.getVersion() != null) {
                    return false;
                }
            } else if (!_version.equals(other.getVersion())) {
                return false;
            }
            if (_xid == null) {
                if (other.getXid() != null) {
                    return false;
                }
            } else if (!_xid.equals(other.getXid())) {
                return false;
            }
            if (getClass() == obj.getClass()) {
                // Simple case: we are comparing against self
                FeaturesRequestMessageImpl otherImpl = (FeaturesRequestMessageImpl) obj;
                if (augmentation == null) {
                    if (otherImpl.augmentation != null) {
                        return false;
                    }
                } else if (!augmentation.equals(otherImpl.augmentation)) {
                    return false;
                }
            } else {
                // Hard case: compare our augments with presence there...
                for (Map.Entry<java.lang.Class<? extends Augmentation<OF10FeaturesRequestMessage>>, Augmentation<OF10FeaturesRequestMessage>> e : augmentation
                        .entrySet()) {
                    if (!e.getValue().equals(other.getAugmentation(e.getKey()))) {
                        return false;
                    }
                }
                // .. and give the other one the chance to do the same
                if (!obj.equals(this)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public java.lang.String toString() {
            java.lang.StringBuilder builder = new java.lang.StringBuilder("FeaturesRequestMessage [");
            boolean first = true;

            if (_version != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_version=");
                builder.append(_version);
            }
            if (_xid != null) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append("_xid=");
                builder.append(_xid);
            }
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append("augmentation=");
            builder.append(augmentation.values());
            return builder.append(']').toString();
        }
    }

    @Override
    public OF10FeaturesRequestMessage toInstance() {
        return new FeaturesRequestMessageImpl(this);
    }

}
