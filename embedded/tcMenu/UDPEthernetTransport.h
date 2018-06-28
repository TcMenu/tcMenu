/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#ifndef _TCMENU_UDPETHERNETTRANSPORT_H_
#define _TCMENU_UDPETHERNETTRANSPORT_H_

#include <RemoteConnector.h>
#include <EthernetUdp.h>

class UDPEthernetTransport : public TagValueTransport {
private:
	bool msgOutputInProgress;
	int deviceId;
	EthernetUDP* net;
public:
	UDPEthernetTransport(EthernetUDP* net, int deviceId);
	virtual ~UDPEthernetTransport();

	virtual void startMsg(uint16_t msgType);

	virtual FieldAndValue* fieldIfAvailable();

	virtual int writeChar(char data);
	virtual int writeStr(const char* data);
	virtual void flush();
	virtual bool available();
	virtual bool connected();
private:
	uint16_t readMsgKeyFollowedBy(char toFollow);
};

#endif /* _TCMENU_UDPETHERNETTRANSPORT_H_ */
