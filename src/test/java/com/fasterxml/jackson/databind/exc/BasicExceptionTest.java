package com.fasterxml.jackson.databind.exc;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class BasicExceptionTest extends BaseMapTest
{
    final ObjectMapper MAPPER = new ObjectMapper();
    final TokenStreamFactory JSON_F = MAPPER.getFactory();

    public void testBadDefinition() throws Exception
    {
        JavaType t = TypeFactory.defaultInstance().constructType(String.class);
        JsonParser p = JSON_F.createParser(ObjectReadContext.empty(), "[]");
        InvalidDefinitionException e = new InvalidDefinitionException(p,
               "Testing", t);
        assertEquals("Testing", e.getOriginalMessage());
        assertEquals(String.class, e.getType().getRawClass());
        assertNull(e.getBeanDescription());
        assertNull(e.getProperty());
        assertSame(p, e.getProcessor());
        p.close();

        // and via factory method:
        BeanDescription beanDef = MAPPER.getSerializationConfig().introspectClassAnnotations(getClass());
        e = InvalidDefinitionException.from(p, "Testing",
                beanDef, (BeanPropertyDefinition) null);
        assertEquals(beanDef.getType(), e.getType());
        assertNotNull(e);
        
        // and the other constructor too
        e = new InvalidDefinitionException(p,
                "Testing", t);
        assertEquals("Testing", e.getOriginalMessage());
        assertEquals(String.class, e.getType().getRawClass());

        // and factory
        JsonGenerator g = JSON_F.createGenerator(ObjectWriteContext.empty(), new StringWriter());
        e = InvalidDefinitionException.from(g, "Testing",
                beanDef, (BeanPropertyDefinition) null);
        assertEquals(beanDef.getType(), e.getType());
        assertNotNull(e);
        
        g.close();
    }

    public void testIgnoredProperty() throws Exception
    {
        // first just construct valid instance with some variations
        JsonParser p = JSON_F.createParser(ObjectReadContext.empty(), "{ }");
        IgnoredPropertyException e = IgnoredPropertyException.from(p,
                this, // to get class from
                "testProp", Collections.<Object>singletonList("x"));
        assertNotNull(e);

        e = IgnoredPropertyException.from(p,
                getClass(),
                "testProp", null);
        assertNotNull(e);
        assertNull(e.getKnownPropertyIds());
        p.close();

        // also, verify failure if null passed for "value"
        try {
            IgnoredPropertyException.from(p, null,
                    "testProp", Collections.<Object>singletonList("x"));
            fail("Should not pass");
        } catch (NullPointerException e2) {
        }
    }

    public void testUnrecognizedProperty() throws Exception
    {
        JsonParser p = JSON_F.createParser(ObjectReadContext.empty(), "{ }");
        UnrecognizedPropertyException e = UnrecognizedPropertyException.from(p, this,
                "testProp", Collections.<Object>singletonList("y"));
        assertNotNull(e);
        assertEquals(getClass(), e.getReferringClass());
        Collection<Object> ids = e.getKnownPropertyIds();
        assertNotNull(ids);
        assertEquals(1, ids.size());
        assertTrue(ids.contains("y"));

        e = UnrecognizedPropertyException.from(p, getClass(),
                "testProp", Collections.<Object>singletonList("y"));

        assertEquals(getClass(), e.getReferringClass());
        p.close();
    }
}
