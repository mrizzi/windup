package org.jboss.windup.graph.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.exception.NonUniqueResultException;

import com.thinkaurelius.titan.core.TitanGraphQuery;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class GraphService<T extends WindupVertexFrame> implements Service<T>
{

    private Class<T> type;
    private GraphContext context;

    public GraphService(GraphContext context, Class<T> type)
    {
        this.context = context;
        this.type = type;
    }

    public static WindupConfigurationModel getConfigurationModel(GraphContext context)
    {
        return new GraphService<>(context, WindupConfigurationModel.class).getUnique();
    }

    @Override
    public void commit()
    {
        this.context.getGraph().commit();
    }

    @Override
    public long count(Iterable<?> obj)
    {
        GremlinPipeline<Iterable<?>, Object> pipe = new GremlinPipeline<Iterable<?>, Object>();
        return pipe.start(obj).count();
    }

    /**
     * Create a new instance of the given {@link WindupVertexFrame} type.
     */
    @Override
    public T create()
    {
        return context.getFramed().addVertex(null, type);
    }

    @Override
    public T create(Object id)
    {
        return context.getFramed().addVertex(id, type);
    }

    public void delete(T frame)
    {
        context.getFramed().removeVertex(frame.asVertex());
    }

    @Override
    public Iterable<T> findAll()
    {
        FramedGraphQuery query = context.getFramed().query();
        query.has(WindupVertexFrame.PROPERTY_TYPE, Text.CONTAINS, type.getAnnotation(TypeValue.class).value());
        return (Iterable<T>) query.vertices(type);
    }

    @Override
    public Iterable<T> findAllByProperties(String[] keys, String[] vals)
    {
        FramedGraphQuery fgq = context.getFramed().query().has("type", Text.CONTAINS, getTypeValueForSearch());

        for (int i = 0, j = keys.length; i < j; i++)
        {
            String key = keys[i];
            String val = vals[i];

            fgq = fgq.has(key, val);
        }

        return fgq.vertices(type);
    }

    @Override
    public Iterable<T> findAllByProperty(String key, Object value)
    {
        return context.getFramed().getVertices(key, value, type);
    }

    @Override
    public Iterable<T> findAllByPropertyMatchingRegex(String key, String... regex)
    {
        if (regex.length == 0)
            return IterablesUtil.emptyIterable();

        final String regexFinal;
        if (regex.length == 1)
        {
            regexFinal = regex[0];
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b(");
            int i = 0;
            for (String value : regex)
            {
                if (i > 0)
                    builder.append("|");
                builder.append(value);
                i++;
            }
            builder.append(")\\b");
            regexFinal = builder.toString();
        }

        return context.getFramed().query().has("type", Text.CONTAINS, getTypeValueForSearch())
                    .has(key, Text.REGEX, regexFinal).vertices(type);
    }

    @Override
    public T getById(Object id)
    {
        return context.getFramed().getVertex(id, type);
    }

    protected T frame(Vertex vertex)
    {
        return getGraphContext().getFramed().frame(vertex, this.getType());
    }

    protected GraphContext getGraphContext()
    {
        return context;
    }

    protected Class<T> getType()
    {
        return type;
    }

    protected TitanGraphQuery getTypedQuery()
    {
        return getGraphContext()
                    .getGraph().query().has("type", Text.CONTAINS, getTypeValueForSearch());
    }

    protected String getTypeValueForSearch()
    {
        TypeValue typeValue = type.getAnnotation(TypeValue.class);
        if (typeValue == null)
            throw new IllegalArgumentException("Must contain annotation 'TypeValue'");
        return typeValue.value();
    }

    @Override
    public T getUnique() throws NonUniqueResultException
    {
        Iterable<T> results = findAll();

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iter = results.iterator();
        T result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    @Override
    public T getUniqueByProperty(String property, Object value) throws NonUniqueResultException
    {
        Iterable<T> results = findAllByProperty(property, value);

        if (!results.iterator().hasNext())
        {
            return null;
        }

        Iterator<T> iter = results.iterator();
        T result = iter.next();

        if (iter.hasNext())
        {
            throw new NonUniqueResultException("Expected unique value, but returned non-unique.");
        }

        return result;
    }

    @Override
    public TitanTransaction newTransaction()
    {
        return context.getGraph().newTransaction();
    }

    public static List<WindupVertexFrame> toVertexFrames(GraphContext graphContext, Iterable<Vertex> vertices)
    {
        List<WindupVertexFrame> results = new ArrayList<>();
        for (Vertex v : vertices)
        {
            WindupVertexFrame frame = graphContext.getFramed().frame(v, WindupVertexFrame.class);
            results.add(frame);
        }
        return results;
    }

    /**
     * Adds the specified type to this frame, and returns a new object that implements this type.
     * 
     * @see GraphTypeManagerTest
     */
    public static <T extends WindupVertexFrame> T addTypeToModel(GraphContext graphContext, WindupVertexFrame frame,
                Class<T> type)
    {
        Vertex vertex = frame.asVertex();
        graphContext.getGraphTypeRegistry().addTypeToElement(type, vertex);
        graphContext.getGraph().commit();
        return graphContext.getFramed().frame(vertex, type);
    }

}
