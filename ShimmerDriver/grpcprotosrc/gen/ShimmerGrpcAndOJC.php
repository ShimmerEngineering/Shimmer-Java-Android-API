<?php
// DO NOT EDIT! Generated by Protobuf-PHP protoc plugin 0.9.4
// Source: src/ShimmerGrpcAndOJC.proto
//   Date: 2016-07-16 10:38:38

namespace shimmerGRPC {

  class HelloRequest extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $name = null;
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.HelloRequest');

      // OPTIONAL STRING name = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "name";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <name> has a value
     *
     * @return boolean
     */
    public function hasName(){
      return $this->_has(1);
    }
    
    /**
     * Clear <name> value
     *
     * @return \shimmerGRPC\HelloRequest
     */
    public function clearName(){
      return $this->_clear(1);
    }
    
    /**
     * Get <name> value
     *
     * @return string
     */
    public function getName(){
      return $this->_get(1);
    }
    
    /**
     * Set <name> value
     *
     * @param string $value
     * @return \shimmerGRPC\HelloRequest
     */
    public function setName( $value){
      return $this->_set(1, $value);
    }
  }
}

namespace shimmerGRPC {

  class HelloReply extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $message = null;
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.HelloReply');

      // OPTIONAL STRING message = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "message";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <message> has a value
     *
     * @return boolean
     */
    public function hasMessage(){
      return $this->_has(1);
    }
    
    /**
     * Clear <message> value
     *
     * @return \shimmerGRPC\HelloReply
     */
    public function clearMessage(){
      return $this->_clear(1);
    }
    
    /**
     * Get <message> value
     *
     * @return string
     */
    public function getMessage(){
      return $this->_get(1);
    }
    
    /**
     * Set <message> value
     *
     * @param string $value
     * @return \shimmerGRPC\HelloReply
     */
    public function setMessage( $value){
      return $this->_set(1, $value);
    }
  }
}

namespace shimmerGRPC {

  class StreamRequest extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $message = null;
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.StreamRequest');

      // OPTIONAL STRING message = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "message";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <message> has a value
     *
     * @return boolean
     */
    public function hasMessage(){
      return $this->_has(1);
    }
    
    /**
     * Clear <message> value
     *
     * @return \shimmerGRPC\StreamRequest
     */
    public function clearMessage(){
      return $this->_clear(1);
    }
    
    /**
     * Get <message> value
     *
     * @return string
     */
    public function getMessage(){
      return $this->_get(1);
    }
    
    /**
     * Set <message> value
     *
     * @param string $value
     * @return \shimmerGRPC\StreamRequest
     */
    public function setMessage( $value){
      return $this->_set(1, $value);
    }
  }
}

namespace shimmerGRPC\ObjectCluster2 {

  class CommunicationType extends \DrSlump\Protobuf\Enum {
    const BT = 0;
    const SD = 1;
    const Radio_802_15_4 = 2;
  }
}
namespace shimmerGRPC\ObjectCluster2\FormatCluster2 {

  class DataCluster2 extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $unit = null;
    
    /**  @var float */
    public $data = null;
    
    /**  @var float[]  */
    public $dataArray = array();
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.ObjectCluster2.FormatCluster2.DataCluster2');

      // OPTIONAL STRING unit = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "unit";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // OPTIONAL DOUBLE data = 2
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 2;
      $f->name      = "data";
      $f->type      = \DrSlump\Protobuf::TYPE_DOUBLE;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // REPEATED DOUBLE dataArray = 3
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 3;
      $f->name      = "dataArray";
      $f->type      = \DrSlump\Protobuf::TYPE_DOUBLE;
      $f->rule      = \DrSlump\Protobuf::RULE_REPEATED;
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <unit> has a value
     *
     * @return boolean
     */
    public function hasUnit(){
      return $this->_has(1);
    }
    
    /**
     * Clear <unit> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function clearUnit(){
      return $this->_clear(1);
    }
    
    /**
     * Get <unit> value
     *
     * @return string
     */
    public function getUnit(){
      return $this->_get(1);
    }
    
    /**
     * Set <unit> value
     *
     * @param string $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function setUnit( $value){
      return $this->_set(1, $value);
    }
    
    /**
     * Check if <data> has a value
     *
     * @return boolean
     */
    public function hasData(){
      return $this->_has(2);
    }
    
    /**
     * Clear <data> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function clearData(){
      return $this->_clear(2);
    }
    
    /**
     * Get <data> value
     *
     * @return float
     */
    public function getData(){
      return $this->_get(2);
    }
    
    /**
     * Set <data> value
     *
     * @param float $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function setData( $value){
      return $this->_set(2, $value);
    }
    
    /**
     * Check if <dataArray> has a value
     *
     * @return boolean
     */
    public function hasDataArray(){
      return $this->_has(3);
    }
    
    /**
     * Clear <dataArray> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function clearDataArray(){
      return $this->_clear(3);
    }
    
    /**
     * Get <dataArray> value
     *
     * @param int $idx
     * @return float
     */
    public function getDataArray($idx = NULL){
      return $this->_get(3, $idx);
    }
    
    /**
     * Set <dataArray> value
     *
     * @param float $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function setDataArray( $value, $idx = NULL){
      return $this->_set(3, $value, $idx);
    }
    
    /**
     * Get all elements of <dataArray>
     *
     * @return float[]
     */
    public function getDataArrayList(){
     return $this->_get(3);
    }
    
    /**
     * Add a new element to <dataArray>
     *
     * @param float $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function addDataArray( $value){
     return $this->_add(3, $value);
    }
  }
}

namespace shimmerGRPC\ObjectCluster2\FormatCluster2 {

  class FormatMapEntry extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $key = null;
    
    /**  @var \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2 */
    public $value = null;
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.ObjectCluster2.FormatCluster2.FormatMapEntry');

      // OPTIONAL STRING key = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "key";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // OPTIONAL MESSAGE value = 2
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 2;
      $f->name      = "value";
      $f->type      = \DrSlump\Protobuf::TYPE_MESSAGE;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $f->reference = '\shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2';
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <key> has a value
     *
     * @return boolean
     */
    public function hasKey(){
      return $this->_has(1);
    }
    
    /**
     * Clear <key> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry
     */
    public function clearKey(){
      return $this->_clear(1);
    }
    
    /**
     * Get <key> value
     *
     * @return string
     */
    public function getKey(){
      return $this->_get(1);
    }
    
    /**
     * Set <key> value
     *
     * @param string $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry
     */
    public function setKey( $value){
      return $this->_set(1, $value);
    }
    
    /**
     * Check if <value> has a value
     *
     * @return boolean
     */
    public function hasValue(){
      return $this->_has(2);
    }
    
    /**
     * Clear <value> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry
     */
    public function clearValue(){
      return $this->_clear(2);
    }
    
    /**
     * Get <value> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2
     */
    public function getValue(){
      return $this->_get(2);
    }
    
    /**
     * Set <value> value
     *
     * @param \shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2 $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry
     */
    public function setValue(\shimmerGRPC\ObjectCluster2\FormatCluster2\DataCluster2 $value){
      return $this->_set(2, $value);
    }
  }
}

namespace shimmerGRPC\ObjectCluster2 {

  class FormatCluster2 extends \DrSlump\Protobuf\Message {

    /**  @var \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry[]  */
    public $formatMap = array();
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.ObjectCluster2.FormatCluster2');

      // REPEATED MESSAGE formatMap = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "formatMap";
      $f->type      = \DrSlump\Protobuf::TYPE_MESSAGE;
      $f->rule      = \DrSlump\Protobuf::RULE_REPEATED;
      $f->reference = '\shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry';
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <formatMap> has a value
     *
     * @return boolean
     */
    public function hasFormatMap(){
      return $this->_has(1);
    }
    
    /**
     * Clear <formatMap> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2
     */
    public function clearFormatMap(){
      return $this->_clear(1);
    }
    
    /**
     * Get <formatMap> value
     *
     * @param int $idx
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry
     */
    public function getFormatMap($idx = NULL){
      return $this->_get(1, $idx);
    }
    
    /**
     * Set <formatMap> value
     *
     * @param \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2
     */
    public function setFormatMap(\shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry $value, $idx = NULL){
      return $this->_set(1, $value, $idx);
    }
    
    /**
     * Get all elements of <formatMap>
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry[]
     */
    public function getFormatMapList(){
     return $this->_get(1);
    }
    
    /**
     * Add a new element to <formatMap>
     *
     * @param \shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry $value
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2
     */
    public function addFormatMap(\shimmerGRPC\ObjectCluster2\FormatCluster2\FormatMapEntry $value){
     return $this->_add(1, $value);
    }
  }
}

namespace shimmerGRPC\ObjectCluster2 {

  class DataMapEntry extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $key = null;
    
    /**  @var \shimmerGRPC\ObjectCluster2\FormatCluster2 */
    public $value = null;
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.ObjectCluster2.DataMapEntry');

      // OPTIONAL STRING key = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "key";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // OPTIONAL MESSAGE value = 2
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 2;
      $f->name      = "value";
      $f->type      = \DrSlump\Protobuf::TYPE_MESSAGE;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $f->reference = '\shimmerGRPC\ObjectCluster2\FormatCluster2';
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <key> has a value
     *
     * @return boolean
     */
    public function hasKey(){
      return $this->_has(1);
    }
    
    /**
     * Clear <key> value
     *
     * @return \shimmerGRPC\ObjectCluster2\DataMapEntry
     */
    public function clearKey(){
      return $this->_clear(1);
    }
    
    /**
     * Get <key> value
     *
     * @return string
     */
    public function getKey(){
      return $this->_get(1);
    }
    
    /**
     * Set <key> value
     *
     * @param string $value
     * @return \shimmerGRPC\ObjectCluster2\DataMapEntry
     */
    public function setKey( $value){
      return $this->_set(1, $value);
    }
    
    /**
     * Check if <value> has a value
     *
     * @return boolean
     */
    public function hasValue(){
      return $this->_has(2);
    }
    
    /**
     * Clear <value> value
     *
     * @return \shimmerGRPC\ObjectCluster2\DataMapEntry
     */
    public function clearValue(){
      return $this->_clear(2);
    }
    
    /**
     * Get <value> value
     *
     * @return \shimmerGRPC\ObjectCluster2\FormatCluster2
     */
    public function getValue(){
      return $this->_get(2);
    }
    
    /**
     * Set <value> value
     *
     * @param \shimmerGRPC\ObjectCluster2\FormatCluster2 $value
     * @return \shimmerGRPC\ObjectCluster2\DataMapEntry
     */
    public function setValue(\shimmerGRPC\ObjectCluster2\FormatCluster2 $value){
      return $this->_set(2, $value);
    }
  }
}

namespace shimmerGRPC {

  class ObjectCluster2 extends \DrSlump\Protobuf\Message {

    /**  @var string */
    public $name = null;
    
    /**  @var string */
    public $bluetoothAddress = null;
    
    /**  @var int - \shimmerGRPC\ObjectCluster2\CommunicationType */
    public $communicationType = null;
    
    /**  @var \shimmerGRPC\ObjectCluster2\DataMapEntry[]  */
    public $dataMap = array();
    
    /**  @var int */
    public $systemTime = null;
    
    /**  @var float */
    public $calibratedTimeStamp = null;
    

    /** @var \Closure[] */
    protected static $__extensions = array();

    public static function descriptor()
    {
      $descriptor = new \DrSlump\Protobuf\Descriptor(__CLASS__, 'shimmerGRPC.ObjectCluster2');

      // OPTIONAL STRING name = 1
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 1;
      $f->name      = "name";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // OPTIONAL STRING bluetoothAddress = 2
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 2;
      $f->name      = "bluetoothAddress";
      $f->type      = \DrSlump\Protobuf::TYPE_STRING;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // OPTIONAL ENUM communicationType = 3
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 3;
      $f->name      = "communicationType";
      $f->type      = \DrSlump\Protobuf::TYPE_ENUM;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $f->reference = '\shimmerGRPC\ObjectCluster2\CommunicationType';
      $descriptor->addField($f);

      // REPEATED MESSAGE dataMap = 4
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 4;
      $f->name      = "dataMap";
      $f->type      = \DrSlump\Protobuf::TYPE_MESSAGE;
      $f->rule      = \DrSlump\Protobuf::RULE_REPEATED;
      $f->reference = '\shimmerGRPC\ObjectCluster2\DataMapEntry';
      $descriptor->addField($f);

      // OPTIONAL INT64 systemTime = 5
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 5;
      $f->name      = "systemTime";
      $f->type      = \DrSlump\Protobuf::TYPE_INT64;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      // OPTIONAL DOUBLE calibratedTimeStamp = 6
      $f = new \DrSlump\Protobuf\Field();
      $f->number    = 6;
      $f->name      = "calibratedTimeStamp";
      $f->type      = \DrSlump\Protobuf::TYPE_DOUBLE;
      $f->rule      = \DrSlump\Protobuf::RULE_OPTIONAL;
      $descriptor->addField($f);

      foreach (self::$__extensions as $cb) {
        $descriptor->addField($cb(), true);
      }

      return $descriptor;
    }

    /**
     * Check if <name> has a value
     *
     * @return boolean
     */
    public function hasName(){
      return $this->_has(1);
    }
    
    /**
     * Clear <name> value
     *
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function clearName(){
      return $this->_clear(1);
    }
    
    /**
     * Get <name> value
     *
     * @return string
     */
    public function getName(){
      return $this->_get(1);
    }
    
    /**
     * Set <name> value
     *
     * @param string $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function setName( $value){
      return $this->_set(1, $value);
    }
    
    /**
     * Check if <bluetoothAddress> has a value
     *
     * @return boolean
     */
    public function hasBluetoothAddress(){
      return $this->_has(2);
    }
    
    /**
     * Clear <bluetoothAddress> value
     *
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function clearBluetoothAddress(){
      return $this->_clear(2);
    }
    
    /**
     * Get <bluetoothAddress> value
     *
     * @return string
     */
    public function getBluetoothAddress(){
      return $this->_get(2);
    }
    
    /**
     * Set <bluetoothAddress> value
     *
     * @param string $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function setBluetoothAddress( $value){
      return $this->_set(2, $value);
    }
    
    /**
     * Check if <communicationType> has a value
     *
     * @return boolean
     */
    public function hasCommunicationType(){
      return $this->_has(3);
    }
    
    /**
     * Clear <communicationType> value
     *
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function clearCommunicationType(){
      return $this->_clear(3);
    }
    
    /**
     * Get <communicationType> value
     *
     * @return int - \shimmerGRPC\ObjectCluster2\CommunicationType
     */
    public function getCommunicationType(){
      return $this->_get(3);
    }
    
    /**
     * Set <communicationType> value
     *
     * @param int - \shimmerGRPC\ObjectCluster2\CommunicationType $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function setCommunicationType( $value){
      return $this->_set(3, $value);
    }
    
    /**
     * Check if <dataMap> has a value
     *
     * @return boolean
     */
    public function hasDataMap(){
      return $this->_has(4);
    }
    
    /**
     * Clear <dataMap> value
     *
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function clearDataMap(){
      return $this->_clear(4);
    }
    
    /**
     * Get <dataMap> value
     *
     * @param int $idx
     * @return \shimmerGRPC\ObjectCluster2\DataMapEntry
     */
    public function getDataMap($idx = NULL){
      return $this->_get(4, $idx);
    }
    
    /**
     * Set <dataMap> value
     *
     * @param \shimmerGRPC\ObjectCluster2\DataMapEntry $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function setDataMap(\shimmerGRPC\ObjectCluster2\DataMapEntry $value, $idx = NULL){
      return $this->_set(4, $value, $idx);
    }
    
    /**
     * Get all elements of <dataMap>
     *
     * @return \shimmerGRPC\ObjectCluster2\DataMapEntry[]
     */
    public function getDataMapList(){
     return $this->_get(4);
    }
    
    /**
     * Add a new element to <dataMap>
     *
     * @param \shimmerGRPC\ObjectCluster2\DataMapEntry $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function addDataMap(\shimmerGRPC\ObjectCluster2\DataMapEntry $value){
     return $this->_add(4, $value);
    }
    
    /**
     * Check if <systemTime> has a value
     *
     * @return boolean
     */
    public function hasSystemTime(){
      return $this->_has(5);
    }
    
    /**
     * Clear <systemTime> value
     *
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function clearSystemTime(){
      return $this->_clear(5);
    }
    
    /**
     * Get <systemTime> value
     *
     * @return int
     */
    public function getSystemTime(){
      return $this->_get(5);
    }
    
    /**
     * Set <systemTime> value
     *
     * @param int $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function setSystemTime( $value){
      return $this->_set(5, $value);
    }
    
    /**
     * Check if <calibratedTimeStamp> has a value
     *
     * @return boolean
     */
    public function hasCalibratedTimeStamp(){
      return $this->_has(6);
    }
    
    /**
     * Clear <calibratedTimeStamp> value
     *
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function clearCalibratedTimeStamp(){
      return $this->_clear(6);
    }
    
    /**
     * Get <calibratedTimeStamp> value
     *
     * @return float
     */
    public function getCalibratedTimeStamp(){
      return $this->_get(6);
    }
    
    /**
     * Set <calibratedTimeStamp> value
     *
     * @param float $value
     * @return \shimmerGRPC\ObjectCluster2
     */
    public function setCalibratedTimeStamp( $value){
      return $this->_set(6, $value);
    }
  }
}

