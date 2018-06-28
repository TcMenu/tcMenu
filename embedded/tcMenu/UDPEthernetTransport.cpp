/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include <UDPEthernetTransport.h>

#define ETHERNET_MAGIC 454356577L
#define TAGVAL_PROTOCOL 1

UDPEthernetTransport::UDPEthernetTransport(EthernetUDP* net, int deviceId) {
	this->net = net;
	this->deviceId = deviceId;
	this->msgOutputInProgress = false;
}

UDPEthernetTransport::~UDPEthernetTransport() {
}

bool UDPEthernetTransport::available() {
	return net->availableForWrite();
}

bool UDPEthernetTransport::connected() {
	return net->available();
}

void write32(EthernetUDP* net, long value) {
	net->write(value >> 24);
	net->write((value >> 16) & 0xff);
	net->write((value >> 8) & 0xff);
	net->write(value & 0xff);
}

void write16(EthernetUDP* net, int value) {
	net->write((value >> 8) & 0xff);
	net->write(value & 0xff);
}

void UDPEthernetTransport::startMsg(uint16_t msgType) {
	if(!msgOutputInProgress) {
		// write the header part
		write32(net, ETHERNET_MAGIC);
		write16(net, deviceId);
		write16(net, TAGVAL_PROTOCOL);
		msgOutputInProgress = true;
	}

	TagValueTransport::startMsg(msgType);
}

long read32(EthernetUDP* net) {
	return (((long)net->read()) << 24) | ((long)net->read()) << 16 | ((long)net->read()) << 8 | net->read();
}

int read16(EthernetUDP* net) {
	return net->read() << 8 | net->read();
}

FieldAndValue* UDPEthernetTransport::fieldIfAvailable() {
	if(currentField.fieldType == FVAL_PROCESSING_AWAITINGMSG || currentField.fieldType == FVAL_ERROR_PROTO) {
		int len = net->parsePacket();
		bool contProcessing = len > 10;
		contProcessing = contProcessing && (read32(net) == ETHERNET_MAGIC);
		contProcessing = contProcessing && (read16(net) == deviceId);
		contProcessing = contProcessing && (read16(net) == TAGVAL_PROTOCOL);

		currentField.fieldType = (contProcessing) ? FVAL_END_MSG : FVAL_PROCESSING_AWAITINGMSG;
	}

	if(currentField.fieldType == FVAL_END_MSG) {
		// make sure there's even a message to process
		if(net->available() < 4) {
			clearFieldStatus(FVAL_PROCESSING_AWAITINGMSG);
			return &currentField;
		}

		// and the start of message must directly follow the previous msg
		bool contProcessing = contProcessing && (net->read() == '`');

		// and the msg type field key must directly follow
		contProcessing = (contProcessing && readMsgKeyFollowedBy('=') == FIELD_MSG_TYPE);

		// and that must be followed by a type character msg type and end of field.
		currentField.msgType = readMsgKeyFollowedBy('|');
		contProcessing = contProcessing && (currentField.msgType != UNKNOWN_MSG_TYPE);
		if(contProcessing) {
			currentField.fieldType = FVAL_PROCESSING;
		}
		else {
			clearFieldStatus(FVAL_ERROR_PROTO);
			return &currentField;
		}
	}

	if(currentField.fieldType == FVAL_NEW_MSG || currentField.fieldType == FVAL_FIELD || currentField.fieldType == FVAL_PROCESSING) {
		currentField.field = readMsgKeyFollowedBy('=');
		if(currentField.field == UNKNOWN_MSG_TYPE) {
			return &currentField;
		}

		uint8_t len = 0;
		char lastRead = net->read();
		while(net->available() > 0 && lastRead != '|' && len < (MAX_VALUE_LEN - 1)) {
			currentField.value[len] = lastRead;
			lastRead = net->read();
			++len;
		}
		currentField.value[len+1] = 0;
		currentField.fieldType = (FVAL_PROCESSING ? FVAL_NEW_MSG : FVAL_FIELD);
	}

	return &currentField;
}

int UDPEthernetTransport::writeChar(char data) {
	return net->write(data);
}

int UDPEthernetTransport::writeStr(const char* data) {
	return net->write(data);
}

void UDPEthernetTransport::flush() {
	if(msgOutputInProgress) net->endPacket();
}

uint16_t UDPEthernetTransport::readMsgKeyFollowedBy(char toFollow) {
	uint8_t hi = net->read();
	if(hi == '~') {
		clearFieldStatus(FVAL_END_MSG);
		return UNKNOWN_MSG_TYPE;
	}
	uint8_t lo = net->read();
	if(msgFieldToWord(hi, lo) != FIELD_MSG_TYPE || net->read() != toFollow) {
		clearFieldStatus(FVAL_ERROR_PROTO);
		return UNKNOWN_MSG_TYPE;
	}
	return msgFieldToWord(hi, lo);
}

