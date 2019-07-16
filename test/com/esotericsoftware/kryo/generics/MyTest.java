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
    public void serialize() {
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
