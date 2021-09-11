module balzac.lib {
    
    exports xyz.balzaclang.lib;
    exports xyz.balzaclang.lib.model;
    exports xyz.balzaclang.lib.model.script;
    exports xyz.balzaclang.lib.model.script.primitives;
    exports xyz.balzaclang.lib.model.transaction;
    exports xyz.balzaclang.lib.utils;
    exports xyz.balzaclang.lib.validation;
    
    requires org.bitcoinj.core;    
    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires org.bouncycastle.provider;
}