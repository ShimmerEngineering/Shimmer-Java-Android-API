/**
 * @fileoverview
 * @enhanceable
 * @public
 */
// GENERATED CODE -- DO NOT EDIT!


goog.provide('proto.shimmerGRPC.HelloReply');
goog.provide('proto.shimmerGRPC.HelloRequest');
goog.provide('proto.shimmerGRPC.ObjectCluster2');
goog.provide('proto.shimmerGRPC.ObjectCluster2.CommunicationType');
goog.provide('proto.shimmerGRPC.ObjectCluster2.FormatCluster2');
goog.provide('proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2');
goog.provide('proto.shimmerGRPC.StreamRequest');

goog.require('jspb.Message');
goog.require('jspb.BinaryReader');
goog.require('jspb.BinaryWriter');


/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.shimmerGRPC.HelloRequest = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.shimmerGRPC.HelloRequest, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.shimmerGRPC.HelloRequest.displayName = 'proto.shimmerGRPC.HelloRequest';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.shimmerGRPC.HelloRequest.prototype.toObject = function(opt_includeInstance) {
  return proto.shimmerGRPC.HelloRequest.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.shimmerGRPC.HelloRequest} msg The msg instance to transform.
 * @return {!Object}
 */
proto.shimmerGRPC.HelloRequest.toObject = function(includeInstance, msg) {
  var f, obj = {
    name: msg.getName()
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.shimmerGRPC.HelloRequest}
 */
proto.shimmerGRPC.HelloRequest.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.shimmerGRPC.HelloRequest;
  return proto.shimmerGRPC.HelloRequest.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.shimmerGRPC.HelloRequest} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.shimmerGRPC.HelloRequest}
 */
proto.shimmerGRPC.HelloRequest.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setName(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Class method variant: serializes the given message to binary data
 * (in protobuf wire format), writing to the given BinaryWriter.
 * @param {!proto.shimmerGRPC.HelloRequest} message
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.HelloRequest.serializeBinaryToWriter = function(message, writer) {
  message.serializeBinaryToWriter(writer);
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.shimmerGRPC.HelloRequest.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  this.serializeBinaryToWriter(writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the message to binary data (in protobuf wire format),
 * writing to the given BinaryWriter.
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.HelloRequest.prototype.serializeBinaryToWriter = function (writer) {
  var f = undefined;
  f = this.getName();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
};


/**
 * Creates a deep clone of this proto. No data is shared with the original.
 * @return {!proto.shimmerGRPC.HelloRequest} The clone.
 */
proto.shimmerGRPC.HelloRequest.prototype.cloneMessage = function() {
  return /** @type {!proto.shimmerGRPC.HelloRequest} */ (jspb.Message.cloneMessage(this));
};


/**
 * optional string name = 1;
 * @return {string}
 */
proto.shimmerGRPC.HelloRequest.prototype.getName = function() {
  return /** @type {string} */ (jspb.Message.getFieldProto3(this, 1, ""));
};


/** @param {string} value  */
proto.shimmerGRPC.HelloRequest.prototype.setName = function(value) {
  jspb.Message.setField(this, 1, value);
};



/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.shimmerGRPC.HelloReply = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.shimmerGRPC.HelloReply, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.shimmerGRPC.HelloReply.displayName = 'proto.shimmerGRPC.HelloReply';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.shimmerGRPC.HelloReply.prototype.toObject = function(opt_includeInstance) {
  return proto.shimmerGRPC.HelloReply.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.shimmerGRPC.HelloReply} msg The msg instance to transform.
 * @return {!Object}
 */
proto.shimmerGRPC.HelloReply.toObject = function(includeInstance, msg) {
  var f, obj = {
    message: msg.getMessage()
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.shimmerGRPC.HelloReply}
 */
proto.shimmerGRPC.HelloReply.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.shimmerGRPC.HelloReply;
  return proto.shimmerGRPC.HelloReply.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.shimmerGRPC.HelloReply} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.shimmerGRPC.HelloReply}
 */
proto.shimmerGRPC.HelloReply.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setMessage(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Class method variant: serializes the given message to binary data
 * (in protobuf wire format), writing to the given BinaryWriter.
 * @param {!proto.shimmerGRPC.HelloReply} message
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.HelloReply.serializeBinaryToWriter = function(message, writer) {
  message.serializeBinaryToWriter(writer);
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.shimmerGRPC.HelloReply.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  this.serializeBinaryToWriter(writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the message to binary data (in protobuf wire format),
 * writing to the given BinaryWriter.
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.HelloReply.prototype.serializeBinaryToWriter = function (writer) {
  var f = undefined;
  f = this.getMessage();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
};


/**
 * Creates a deep clone of this proto. No data is shared with the original.
 * @return {!proto.shimmerGRPC.HelloReply} The clone.
 */
proto.shimmerGRPC.HelloReply.prototype.cloneMessage = function() {
  return /** @type {!proto.shimmerGRPC.HelloReply} */ (jspb.Message.cloneMessage(this));
};


/**
 * optional string message = 1;
 * @return {string}
 */
proto.shimmerGRPC.HelloReply.prototype.getMessage = function() {
  return /** @type {string} */ (jspb.Message.getFieldProto3(this, 1, ""));
};


/** @param {string} value  */
proto.shimmerGRPC.HelloReply.prototype.setMessage = function(value) {
  jspb.Message.setField(this, 1, value);
};



/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.shimmerGRPC.StreamRequest = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.shimmerGRPC.StreamRequest, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.shimmerGRPC.StreamRequest.displayName = 'proto.shimmerGRPC.StreamRequest';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.shimmerGRPC.StreamRequest.prototype.toObject = function(opt_includeInstance) {
  return proto.shimmerGRPC.StreamRequest.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.shimmerGRPC.StreamRequest} msg The msg instance to transform.
 * @return {!Object}
 */
proto.shimmerGRPC.StreamRequest.toObject = function(includeInstance, msg) {
  var f, obj = {
    message: msg.getMessage()
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.shimmerGRPC.StreamRequest}
 */
proto.shimmerGRPC.StreamRequest.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.shimmerGRPC.StreamRequest;
  return proto.shimmerGRPC.StreamRequest.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.shimmerGRPC.StreamRequest} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.shimmerGRPC.StreamRequest}
 */
proto.shimmerGRPC.StreamRequest.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setMessage(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Class method variant: serializes the given message to binary data
 * (in protobuf wire format), writing to the given BinaryWriter.
 * @param {!proto.shimmerGRPC.StreamRequest} message
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.StreamRequest.serializeBinaryToWriter = function(message, writer) {
  message.serializeBinaryToWriter(writer);
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.shimmerGRPC.StreamRequest.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  this.serializeBinaryToWriter(writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the message to binary data (in protobuf wire format),
 * writing to the given BinaryWriter.
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.StreamRequest.prototype.serializeBinaryToWriter = function (writer) {
  var f = undefined;
  f = this.getMessage();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
};


/**
 * Creates a deep clone of this proto. No data is shared with the original.
 * @return {!proto.shimmerGRPC.StreamRequest} The clone.
 */
proto.shimmerGRPC.StreamRequest.prototype.cloneMessage = function() {
  return /** @type {!proto.shimmerGRPC.StreamRequest} */ (jspb.Message.cloneMessage(this));
};


/**
 * optional string message = 1;
 * @return {string}
 */
proto.shimmerGRPC.StreamRequest.prototype.getMessage = function() {
  return /** @type {string} */ (jspb.Message.getFieldProto3(this, 1, ""));
};


/** @param {string} value  */
proto.shimmerGRPC.StreamRequest.prototype.setMessage = function(value) {
  jspb.Message.setField(this, 1, value);
};



/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.shimmerGRPC.ObjectCluster2 = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.shimmerGRPC.ObjectCluster2, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.shimmerGRPC.ObjectCluster2.displayName = 'proto.shimmerGRPC.ObjectCluster2';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.toObject = function(opt_includeInstance) {
  return proto.shimmerGRPC.ObjectCluster2.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.shimmerGRPC.ObjectCluster2} msg The msg instance to transform.
 * @return {!Object}
 */
proto.shimmerGRPC.ObjectCluster2.toObject = function(includeInstance, msg) {
  var f, obj = {
    name: msg.getName(),
    bluetoothaddress: msg.getBluetoothaddress(),
    communicationtype: msg.getCommunicationtype(),
    datamapMap: (f = msg.getDatamapMap(true)) ? f.toArray() : [],
    systemtime: msg.getSystemtime(),
    calibratedtimestamp: msg.getCalibratedtimestamp()
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.shimmerGRPC.ObjectCluster2}
 */
proto.shimmerGRPC.ObjectCluster2.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.shimmerGRPC.ObjectCluster2;
  return proto.shimmerGRPC.ObjectCluster2.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.shimmerGRPC.ObjectCluster2} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.shimmerGRPC.ObjectCluster2}
 */
proto.shimmerGRPC.ObjectCluster2.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setName(value);
      break;
    case 2:
      var value = /** @type {string} */ (reader.readString());
      msg.setBluetoothaddress(value);
      break;
    case 3:
      var value = /** @type {!proto.shimmerGRPC.ObjectCluster2.CommunicationType} */ (reader.readEnum());
      msg.setCommunicationtype(value);
      break;
    case 4:
      var value = msg.getDatamapMap();
      reader.readMessage(value, jspb.Map.deserializeBinary);
      break;
    case 5:
      var value = /** @type {number} */ (reader.readInt64());
      msg.setSystemtime(value);
      break;
    case 6:
      var value = /** @type {number} */ (reader.readDouble());
      msg.setCalibratedtimestamp(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Class method variant: serializes the given message to binary data
 * (in protobuf wire format), writing to the given BinaryWriter.
 * @param {!proto.shimmerGRPC.ObjectCluster2} message
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.ObjectCluster2.serializeBinaryToWriter = function(message, writer) {
  message.serializeBinaryToWriter(writer);
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  this.serializeBinaryToWriter(writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the message to binary data (in protobuf wire format),
 * writing to the given BinaryWriter.
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.ObjectCluster2.prototype.serializeBinaryToWriter = function (writer) {
  var f = undefined;
  f = this.getName();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
  f = this.getBluetoothaddress();
  if (f.length > 0) {
    writer.writeString(
      2,
      f
    );
  }
  f = this.getCommunicationtype();
  if (f !== 0.0) {
    writer.writeEnum(
      3,
      f
    );
  }
  f = this.getDatamapMap(true);
  if (f && f.getLength() > 0) {
    f.serializeBinary(4, writer);
  }
  f = this.getSystemtime();
  if (f !== 0) {
    writer.writeInt64(
      5,
      f
    );
  }
  f = this.getCalibratedtimestamp();
  if (f !== 0.0) {
    writer.writeDouble(
      6,
      f
    );
  }
};


/**
 * Creates a deep clone of this proto. No data is shared with the original.
 * @return {!proto.shimmerGRPC.ObjectCluster2} The clone.
 */
proto.shimmerGRPC.ObjectCluster2.prototype.cloneMessage = function() {
  return /** @type {!proto.shimmerGRPC.ObjectCluster2} */ (jspb.Message.cloneMessage(this));
};


/**
 * optional string name = 1;
 * @return {string}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.getName = function() {
  return /** @type {string} */ (jspb.Message.getFieldProto3(this, 1, ""));
};


/** @param {string} value  */
proto.shimmerGRPC.ObjectCluster2.prototype.setName = function(value) {
  jspb.Message.setField(this, 1, value);
};


/**
 * optional string bluetoothAddress = 2;
 * @return {string}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.getBluetoothaddress = function() {
  return /** @type {string} */ (jspb.Message.getFieldProto3(this, 2, ""));
};


/** @param {string} value  */
proto.shimmerGRPC.ObjectCluster2.prototype.setBluetoothaddress = function(value) {
  jspb.Message.setField(this, 2, value);
};


/**
 * optional CommunicationType communicationType = 3;
 * @return {!proto.shimmerGRPC.ObjectCluster2.CommunicationType}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.getCommunicationtype = function() {
  return /** @type {!proto.shimmerGRPC.ObjectCluster2.CommunicationType} */ (jspb.Message.getFieldProto3(this, 3, 0));
};


/** @param {!proto.shimmerGRPC.ObjectCluster2.CommunicationType} value  */
proto.shimmerGRPC.ObjectCluster2.prototype.setCommunicationtype = function(value) {
  jspb.Message.setField(this, 3, value);
};


/**
 * map<string, FormatCluster2> dataMap = 4;
 * @param {boolean=} opt_noLazyCreate Do not create the map if
 * empty, instead returning `undefined`
 * @return {!jspb.Map<string,!proto.shimmerGRPC.ObjectCluster2.FormatCluster2>}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.getDatamapMap = function(opt_noLazyCreate) {
  return /** @type {!jspb.Map<string,!proto.shimmerGRPC.ObjectCluster2.FormatCluster2>} */ (
      jspb.Message.getMapField(this, 4, opt_noLazyCreate,
      proto.shimmerGRPC.ObjectCluster2.FormatCluster2,
      jspb.BinaryWriter.prototype.writeString,
      jspb.BinaryReader.prototype.readString,
      jspb.BinaryWriter.prototype.writeMessage,
      jspb.BinaryReader.prototype.readMessage,
      proto.shimmerGRPC.ObjectCluster2.FormatCluster2.serializeBinaryToWriter,
      proto.shimmerGRPC.ObjectCluster2.FormatCluster2.deserializeBinaryFromReader));
};


/**
 * optional int64 systemTime = 5;
 * @return {number}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.getSystemtime = function() {
  return /** @type {number} */ (jspb.Message.getFieldProto3(this, 5, 0));
};


/** @param {number} value  */
proto.shimmerGRPC.ObjectCluster2.prototype.setSystemtime = function(value) {
  jspb.Message.setField(this, 5, value);
};


/**
 * optional double calibratedTimeStamp = 6;
 * @return {number}
 */
proto.shimmerGRPC.ObjectCluster2.prototype.getCalibratedtimestamp = function() {
  return /** @type {number} */ (jspb.Message.getFieldProto3(this, 6, 0));
};


/** @param {number} value  */
proto.shimmerGRPC.ObjectCluster2.prototype.setCalibratedtimestamp = function(value) {
  jspb.Message.setField(this, 6, value);
};


/**
 * @enum {number}
 */
proto.shimmerGRPC.ObjectCluster2.CommunicationType = {
  BT: 0,
  SD: 1,
  RADIO_802_15_4: 2
};


/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2 = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.shimmerGRPC.ObjectCluster2.FormatCluster2, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.shimmerGRPC.ObjectCluster2.FormatCluster2.displayName = 'proto.shimmerGRPC.ObjectCluster2.FormatCluster2';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.prototype.toObject = function(opt_includeInstance) {
  return proto.shimmerGRPC.ObjectCluster2.FormatCluster2.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2} msg The msg instance to transform.
 * @return {!Object}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.toObject = function(includeInstance, msg) {
  var f, obj = {
    formatmapMap: (f = msg.getFormatmapMap(true)) ? f.toArray() : []
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.shimmerGRPC.ObjectCluster2.FormatCluster2;
  return proto.shimmerGRPC.ObjectCluster2.FormatCluster2.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = msg.getFormatmapMap();
      reader.readMessage(value, jspb.Map.deserializeBinary);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Class method variant: serializes the given message to binary data
 * (in protobuf wire format), writing to the given BinaryWriter.
 * @param {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2} message
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.serializeBinaryToWriter = function(message, writer) {
  message.serializeBinaryToWriter(writer);
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  this.serializeBinaryToWriter(writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the message to binary data (in protobuf wire format),
 * writing to the given BinaryWriter.
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.prototype.serializeBinaryToWriter = function (writer) {
  var f = undefined;
  f = this.getFormatmapMap(true);
  if (f && f.getLength() > 0) {
    f.serializeBinary(1, writer);
  }
};


/**
 * Creates a deep clone of this proto. No data is shared with the original.
 * @return {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2} The clone.
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.prototype.cloneMessage = function() {
  return /** @type {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2} */ (jspb.Message.cloneMessage(this));
};


/**
 * map<string, DataCluster2> formatMap = 1;
 * @param {boolean=} opt_noLazyCreate Do not create the map if
 * empty, instead returning `undefined`
 * @return {!jspb.Map<string,!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2>}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.prototype.getFormatmapMap = function(opt_noLazyCreate) {
  return /** @type {!jspb.Map<string,!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2>} */ (
      jspb.Message.getMapField(this, 1, opt_noLazyCreate,
      proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2,
      jspb.BinaryWriter.prototype.writeString,
      jspb.BinaryReader.prototype.readString,
      jspb.BinaryWriter.prototype.writeMessage,
      jspb.BinaryReader.prototype.readMessage,
      proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.serializeBinaryToWriter,
      proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.deserializeBinaryFromReader));
};



/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2 = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.repeatedFields_, null);
};
goog.inherits(proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.displayName = 'proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2';
}
/**
 * List of repeated fields within this message type.
 * @private {!Array<number>}
 * @const
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.repeatedFields_ = [3];



if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.toObject = function(opt_includeInstance) {
  return proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2} msg The msg instance to transform.
 * @return {!Object}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.toObject = function(includeInstance, msg) {
  var f, obj = {
    unit: msg.getUnit(),
    data: msg.getData(),
    dataarrayList: jspb.Message.getRepeatedFloatingPointField(msg, 3)
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2;
  return proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setUnit(value);
      break;
    case 2:
      var value = /** @type {number} */ (reader.readDouble());
      msg.setData(value);
      break;
    case 3:
      var value = /** @type {!Array.<number>} */ (reader.readPackedDouble());
      msg.setDataarrayList(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Class method variant: serializes the given message to binary data
 * (in protobuf wire format), writing to the given BinaryWriter.
 * @param {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2} message
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.serializeBinaryToWriter = function(message, writer) {
  message.serializeBinaryToWriter(writer);
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  this.serializeBinaryToWriter(writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the message to binary data (in protobuf wire format),
 * writing to the given BinaryWriter.
 * @param {!jspb.BinaryWriter} writer
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.serializeBinaryToWriter = function (writer) {
  var f = undefined;
  f = this.getUnit();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
  f = this.getData();
  if (f !== 0.0) {
    writer.writeDouble(
      2,
      f
    );
  }
  f = this.getDataarrayList();
  if (f.length > 0) {
    writer.writePackedDouble(
      3,
      f
    );
  }
};


/**
 * Creates a deep clone of this proto. No data is shared with the original.
 * @return {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2} The clone.
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.cloneMessage = function() {
  return /** @type {!proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2} */ (jspb.Message.cloneMessage(this));
};


/**
 * optional string unit = 1;
 * @return {string}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.getUnit = function() {
  return /** @type {string} */ (jspb.Message.getFieldProto3(this, 1, ""));
};


/** @param {string} value  */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.setUnit = function(value) {
  jspb.Message.setField(this, 1, value);
};


/**
 * optional double data = 2;
 * @return {number}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.getData = function() {
  return /** @type {number} */ (jspb.Message.getFieldProto3(this, 2, 0));
};


/** @param {number} value  */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.setData = function(value) {
  jspb.Message.setField(this, 2, value);
};


/**
 * repeated double dataArray = 3;
 * If you change this array by adding, removing or replacing elements, or if you
 * replace the array itself, then you must call the setter to update it.
 * @return {!Array.<number>}
 */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.getDataarrayList = function() {
  return /** @type {!Array.<number>} */ (jspb.Message.getRepeatedFloatingPointField(this, 3));
};


/** @param {Array.<number>} value  */
proto.shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2.prototype.setDataarrayList = function(value) {
  jspb.Message.setField(this, 3, value || []);
};

