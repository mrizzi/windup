package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This represents Java source code as an abstract concept. This is useful for things that are not strictly ".java" source code, but are semantically
 * very similar to Java source code. Generally files like this will have a 1-to-1 relationship with a generated .java file, but we may only have
 * access to the original source at runtime.
 * <p>
 * An example of this would be a JSP file, although there could be others.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(AbstractJavaSourceModel.TYPE)
public interface AbstractJavaSourceModel extends FileModel, SourceFileModel {
    String TYPE = "AbstractJavaSourceModel";
    String PACKAGE_NAME = "packageName";
    String JAVA_CLASS_MODEL = "javaClass";
    String ROOT_SOURCE_FOLDER = "rootSourceFolder";
    String LINE_MAPPING = "lineMapping";

    /**
     * This is the "root" directory for this source file.
     * <p>
     * For example, if you have a file at "/project/src/main/java/org/example/Foo.java" then this would point the directory "/project/src/main/java".
     */
    @Adjacency(label = ROOT_SOURCE_FOLDER, direction = Direction.OUT)
    FileModel getRootSourceFolder();

    /**
     * This is the "root" directory for this source file.
     * <p>
     * For example, if you have a file at "/project/src/main/java/org/example/Foo.java" then this would point the directory "/project/src/main/java".
     */
    @Adjacency(label = ROOT_SOURCE_FOLDER, direction = Direction.OUT)
    void setRootSourceFolder(FileModel fileModel);

    /**
     * Contains the Java package name
     */
    @Indexed
    @Property(PACKAGE_NAME)
    String getPackageName();

    /**
     * Contains the Java package name
     */
    @Property(PACKAGE_NAME)
    void setPackageName(String packageName);

    /**
     * Lists the {@link JavaClassModel}s contained within this source file
     */
    @Adjacency(label = JAVA_CLASS_MODEL, direction = Direction.OUT)
    List<JavaClassModel> getJavaClasses();

    /**
     * Lists the {@link JavaClassModel}s contained within this source file
     */
    @Adjacency(label = JAVA_CLASS_MODEL, direction = Direction.OUT)
    void addJavaClass(JavaClassModel javaClassModel);

    /**
     * Gets all type references.
     * <p>
     * This uses a default method as the edge label is overloaded. We have to filter it down to insure
     * that only objects of the type that we want are actually returned.
     */
    default List<FileReferenceModel> getAllTypeReferences() {
        return getAllTypeReferencesInternal().stream()
                .filter(frame -> frame instanceof FileReferenceModel)
                .map(frame -> (FileReferenceModel) frame)
                .collect(Collectors.toList());
    }

    /**
     * This method should not be called directly and should effectively be private.
     */
    @Adjacency(label = FileReferenceModel.FILE_MODEL, direction = Direction.IN)
    List<WindupVertexFrame> getAllTypeReferencesInternal();

    /**
     * Line mapping between original and decompiled source code
     */
    @Adjacency(label = LINE_MAPPING, direction = Direction.OUT)
    LineMappingModel getLineMapping();

    /**
     * Line mapping between original and decompiled source code
     */
    @Adjacency(label = LINE_MAPPING, direction = Direction.OUT)
    void setLineMapping(LineMappingModel model);

}
