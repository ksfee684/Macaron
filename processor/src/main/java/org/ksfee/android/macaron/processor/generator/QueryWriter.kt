package org.ksfee.android.macaron.processor.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.ksfee.android.macaron.processor.generator.ext.asKotlinType
import org.ksfee.android.macaron.processor.generator.ext.fieldName
import org.ksfee.android.macaron.processor.generator.ext.isNullable
import org.ksfee.android.macaron.processor.generator.model.CollectionModel
import org.ksfee.android.macaron.processor.generator.util.Types

class QueryWriter(
    private val model: CollectionModel
) : MacaronWriter(model.context) {

    private val fileName: String = model.className + QUERY_SUFFIX

    override fun buildFileSpec(): FileSpec.Builder =
        FileSpec.builder(model.packageName, fileName).apply {
            addImport(Types.FirestoreQuery.packageName, Types.FirestoreQuery.simpleName)

            val queryType = ClassName(model.packageName, fileName)
            val collectionQuery = CollectionQueryWriter(model, queryType)
            val documentQuery = DocumentQueryWriter(model, queryType)

            addType(buildQueryType(
                ClassName(model.packageName, collectionQuery.className),
                ClassName(model.packageName, documentQuery.className)
            ))
            addType(collectionQuery.buildCollectionQueryType())
            addType(documentQuery.buildDocumentQueryType())
        }

    private fun buildQueryType(collectionQuery: TypeName, documentQuery: TypeName): TypeSpec =
        TypeSpec.objectBuilder(fileName).apply {
            addProperty(
                PropertySpec.builder("collection", collectionQuery).apply {
                    getter(FunSpec.getterBuilder().addStatement("return %T()", collectionQuery).build())
                }.build()
            )
            addProperty(
                PropertySpec.builder("document", documentQuery).apply {
                    getter(FunSpec.getterBuilder().addStatement("return %T()", documentQuery).build())
                }.build()
            )
            addFunction(buildDeserializeFunc())
        }.build()

    private fun buildDeserializeFunc(): FunSpec {
        val parameterArgs = mutableListOf<Any>(model.type)
        val parameters = model.fields
            .apply {
                forEach { field ->
                    val retrieveMethod = if (field.isNullable()) {
                        MemberName(
                            Types.TypedNullableValue.packageName,
                            Types.TypedNullableValue.simpleName
                        )
                    } else {
                        MemberName(Types.TypedValue.packageName, Types.TypedValue.simpleName)
                    }
                    parameterArgs.add(retrieveMethod)
                    parameterArgs.add(field.asKotlinType())
                    parameterArgs.add(field.fieldName())
                }
            }
            .joinToString(", ") { "${it.simpleName} = data.%M<%T>(%S)" }

        return FunSpec.builder("deserialize").apply {
            returns(model.type)
            addParameter("reference", Types.DocumentReference)
            addParameter(
                "data",
                Map::class.asClassName().parameterizedBy(
                    String::class.asTypeName(),
                    Any::class.asTypeName()
                )
            )
            addStatement(
                "return %T($parameters).apply { documentReference = reference }",
                *parameterArgs.toTypedArray()
            )
        }.build()
    }

    companion object {
        private const val QUERY_SUFFIX = "Query"
    }
}

class CollectionQueryWriter(
    private val model: CollectionModel,
    private val queryType: TypeName
) {

    val className: String = model.className + QUERY_CLASS_SUFFIX

    fun buildCollectionQueryType(): TypeSpec = TypeSpec.classBuilder(className).apply {
        // super
        superclass(Types.Controller.CollectionQuery.parameterizedBy(model.type))
        addSuperclassConstructorParameter("%S", model.collectionPath)

        // constructor
        primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.INTERNAL).build())

        // function
        addFunctions(buildWhereEqualToFuncs())
        addFunctions(buildOrderByFuncs())
        addFunction(buildDeserializeFunc())
    }.build()

    private fun buildDeserializeFunc(): FunSpec =
        FunSpec.builder("deserialize").apply {
            returns(model.type)
            addModifiers(KModifier.OVERRIDE)
            addParameter("documentSnapshot", Types.QueryDocumentSnapshot)
            addStatement(
                "return %T.deserialize(documentSnapshot.reference, documentSnapshot.data)",
                queryType
            )
        }.build()

    private fun buildWhereEqualToFuncs(): List<FunSpec> =
        model.fields.map {field ->
            FunSpec.builder("${field.simpleName}EqualTo").apply {
                addParameter(field.simpleName.toString(), field.asKotlinType())
                addStatement(
                    "return apply { query.whereEqualTo(%S, ${field.simpleName}) }",
                    field.fieldName()
                )
            }.build()
        }

    private fun buildOrderByFuncs(): List<FunSpec> =
        model.fields.map {field ->
            FunSpec.builder("orderBy${field.simpleName.toString().capitalize()}").apply {
                addParameter(
                    ParameterSpec.builder("direction", Types.Direction).apply {
                        defaultValue("Query.Direction.ASCENDING")
                    }.build()
                )
                beginControlFlow("return apply")
                addStatement("query.orderBy(%S, direction)", field.fieldName())
                endControlFlow()
            }.build()
        }

    companion object {
        private const val QUERY_CLASS_SUFFIX = "CollectionQuery"
    }
}

class DocumentQueryWriter(
    private val model: CollectionModel,
    private val queryType: TypeName
) {

    val className: String = model.className + DOCUMENT_QUERY_SUFFIX

    fun buildDocumentQueryType(): TypeSpec =
        TypeSpec.classBuilder(className).apply {
            // super
            superclass(
                Types.Controller.DocumentQuery.parameterizedBy(model.type)
            )
            addSuperclassConstructorParameter("%S", model.collectionPath)

            // constructor
            primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.INTERNAL).build())

            // function
            addFunction(buildDeserializeFunc())
        }.build()

    private fun buildDeserializeFunc(): FunSpec =
        FunSpec.builder("deserialize").apply {
            addModifiers(KModifier.OVERRIDE)
            returns(model.type)
            addParameter("documentSnapshot", Types.DocumentSnapshot)
            addStatement("val data = documentSnapshot.data ?: throw %T()", Types.Exception.DocumentNotFoundException)
            addStatement("return %T.deserialize(documentSnapshot.reference, data)", queryType)
        }.build()

    companion object {
        private const val DOCUMENT_QUERY_SUFFIX = "DocumentQuery"
    }
}
