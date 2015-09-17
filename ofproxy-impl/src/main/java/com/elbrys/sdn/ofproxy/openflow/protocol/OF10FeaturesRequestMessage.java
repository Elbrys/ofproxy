package com.elbrys.sdn.ofproxy.openflow.protocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesRequest;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;

public interface OF10FeaturesRequestMessage
    extends
    DataObject,
    Augmentable<OF10FeaturesRequestMessage>,
    FeaturesRequest,
    Notification
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:openflow:protocol","2013-07-31","features-request-message"));

}

