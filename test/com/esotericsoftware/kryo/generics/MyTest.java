package com.esotericsoftware.kryo.generics;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.junit.Assert;
import org.junit.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.function.Supplier;
public class MyTest {
    static private final ThreadLocal<Kryo> kryos = new ThreadLocal<Kryo>() {
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            // Configure the Kryo instance.
            kryo.setRegistrationRequired(false);
            kryo.setReferences(true);
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            return kryo;
        };
    };

    // https://github.com/EsotericSoftware/kryo/issues/654
    @Test
    public void test_Serialize() {
        Kryo kryo = kryos.get();

        final Output output = new Output(1024);
        kryo.writeClassAndObject(output, new StringSupplierContainer());
        final byte[] result = output.toBytes();
        Assert.assertNotNull(result);

        Input input = new Input(1024);
        input.setInputStream(new ByteArrayInputStream(result));
        StringSupplierContainer container = (StringSupplierContainer) kryo.readClassAndObject(input);
        Assert.assertNotNull(container);
    }

    @Test
    public void test_MyMap() {
        MyMap<Integer, String, String> myMap = new MyMap<>(new MyEntry<>(1, "ai"), "haha");
        Kryo kryo = kryos.get();
        final Output output = new Output(1024);
        kryo.writeClassAndObject(output, myMap);
        final byte[] result = output.toBytes();
        Assert.assertNotNull(result);

        Input input = new Input(1024);
        input.setInputStream(new ByteArrayInputStream(result));
        MyMap<Integer, String, String> myMap1 = (MyMap<Integer, String, String>) kryo.readClassAndObject(input);
        Assert.assertEquals(myMap.toString(), myMap1.toString());
    }
    static class MyEntry<K, V>{
        K key;
        V value;
        public MyEntry(){}
        public MyEntry(K key, V value){
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "MyEntry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }
    }
    static class MyMap<K, V, C>{
        MyEntry<K, V> node;
        C msg;
        public MyMap(){}

        public MyMap(MyEntry<K, V> node, C msg) {
            this.node = node;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "MyMap{" +
                    "node=" + node +
                    ", msg=" + msg +
                    '}';
        }
    }
    static class EmptyStringSupplier implements Supplier<String>, Serializable {

        @Override
        public String get() {
            return "";
        }
    }

    static class StringSupplierContainer extends SupplierContainer<String> {

        StringSupplierContainer() {
            super(new EmptyStringSupplier());
        }
    }

    static class SupplierContainer<T> {

        private final Supplier<T> input;

        SupplierContainer(Supplier<T> input) {
            this.input = input;
        }
    }
}
