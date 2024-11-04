import java.io.File

if (args.size < 2) {
    throw Exception("Usage: <annotation_name> <path_to_class>")
}

val annotationName = args[0]
val classPath = args[1]

if (classPath.isEmpty()) {
    throw Exception("Error: Invalid path")
}

val sourceFile = File(classPath)

// Validate input
if (sourceFile.exists().not()) {
    throw Exception("Error: The specified file does not exist.")
}

// Get module and package name
val packageName = extractPackageName(classPath)

val gradleFilePath = getBuildGradleFile(classPath)

if (gradleFilePath.isEmpty()) {
    throw Exception("Error: Cannot determine gradle file of the provided module")
}

// Check for required dependencies
if (checkDependencies(gradleFilePath).not()) {
    throw Exception("Error: Required dependencies for unit testing are not added.")
}

val annotationRegex = Regex("""@$annotationName\s+(?:private|public|protected|internal)?\s*fun\s+(\w+)\(([^)]*)\):\s*(\w+)""")

val fileContent = sourceFile.readText()

val testFunctions = mutableListOf<String>()

println("Finding functions...")

annotationRegex.findAll(fileContent).toList().map { matchResult ->

    val functionName = matchResult.groupValues[1]
    val parameters = matchResult.groupValues[2]
    val paramNames = parameters.split(",").map { it.trim().split(" ").getOrNull(0)?.replace(":","") }?.joinToString(", ") ?: ""

    testFunctions.add(createTestFunction(functionName, paramNames))
}

if (testFunctions.isNullOrEmpty()) {
    throw Exception("Error: No function found to generate unit tests")
}

// Create a new test file
println("Generating Unit Tests...")

val testFilePath = createTestFile(classPath, testFunctions.joinToString("\n\n"), sourceFile)

println("Test file created at: $testFilePath")

/**
 * Creates a test function with the specified function name and parameter list.
 *
 * @param functionName The name of the function to be tested.
 * @param paramNames Comma-separated names of parameters to pass into the function.
 * @return A string representation of the generated test function.
 */
fun createTestFunction(functionName: String, paramNames: String): String {
    val indent = "    ".repeat(1) // Each indent level adds 4 spaces
    return buildString {
        appendLine("${indent}@Test")
        appendLine("${indent}fun ${functionName.camelcase()}Test() {")
        appendLine("$indent    //TODO: val result = $functionName($paramNames)")
        appendLine("$indent    //TODO: Add logic for your test")
        appendLine("$indent    //TODO: Assert the result is expected")
        appendLine("$indent}")
    }.toString()
}

/**
 * Extracts the package name from the file path by analyzing the directory structure.
 *
 * @param classPath The path to the class file.
 * @return The extracted package name as a string.
 * @throws Exception if the file path does not contain `src/main/java` or `src/main/kotlin`.
 */
fun extractPackageName(classPath: String): String {
    val seperator = if (classPath.contains("src/main/kotlin")) {
        "src/main/kotlin"
    } else if (classPath.contains("src/main/java")) {
        "src/main/java"
    } else {
        ""
    }

    if (seperator.isEmpty()) {
        throw Exception("Error: file path should contain src/main/java(or kotlin)")
    }

    val packagePath = classPath.split(seperator)[1]
    val fileName = File(classPath).name
    val packagePathWithoutFileName = packagePath.replace("/$fileName","")

    return packagePathWithoutFileName.replace("/",".").replaceFirst(".","")
}

/**
 * Retrieves the build.gradle file path associated with the specified module.
 *
 * @param classPath The path to the source file.
 * @return The file path to the build.gradle file.
 * @throws Exception if the provided path does not contain `/src`.
 */
fun getBuildGradleFile(classPath: String): String {
    val srcParentPath = classPath.split("/src").getOrNull(0)
    if (srcParentPath.isNullOrEmpty()) {
        throw Exception("Error: file path should contain /src")
    }

    val dir = File(srcParentPath)

    if (dir.exists().not() || dir.isDirectory.not()) {
        return ""
    }

    val fileNames = dir.listFiles().map { it.name }

    val name = if (fileNames.contains("build.gradle")) {
        "build.gradle"
    } else if (fileNames.contains("build.gradle.kts")) {
        "build.gradle.kts"
    } else {
        ""
    }

    return if (name.isNotEmpty()) {
        "$srcParentPath/$name"
    } else {
        ""
    }
}

/**
 * Checks if required dependencies are included in the provided build.gradle file.
 *
 * @param gradleFilePath The path to the build.gradle file.
 * @return True if the file contains `testImplementation` dependencies, false otherwise.
 */
fun checkDependencies(gradleFilePath: String): Boolean {
    val buildFile = File(gradleFilePath)
    return if (buildFile.exists()) {
        val content = buildFile.readText()
        content.contains("testImplementation")
    } else {
        false
    }
}

/**
 * Creates a new test file in the appropriate test directory, generating test functions based on the source file.
 *
 * @param classPath The path to the source file.
 * @param testFunctions The string containing all generated test functions.
 * @param originalFile The original source file for reference.
 * @return The file path to the generated test file.
 * @throws Exception if the test file cannot be created.
 */
fun createTestFile(classPath: String, testFunctions: String, originalFile: File): String {
    val testFileName = originalFile.nameWithoutExtension + "Test.kt"
    val testDirPath = classPath.split(originalFile.name).getOrNull(0)?.replace("main","test") ?: ""
    if (testDirPath.isNullOrEmpty()) {
        throw Exception("Error: Cannot create test file")
    }

    val testDir = File(testDirPath)

    if (!testDir.exists()) {
        testDir.mkdirs()
    }

    val testFile = testDir.resolve(testFileName)
    testFile.writeText("""
package ${extractPackageName(originalFile.path)} 

import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ${testFileName}, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ${originalFile.nameWithoutExtension}Test {
    @Before
    fun setup() {
        //TODO: setup prerequisites for tests
    }

    @After
    fun tearDown() {
        //TODO: clear resources after test
    }

$testFunctions
}
    """.trimIndent())
    return testFile.path
}

/**
 * Converts the string to camelCase format, typically for naming conventions in test functions.
 *
 * @receiver The string to be converted.
 * @return The string in camelCase format.
 */
fun String.camelcase(): String {
    return this.replaceFirstChar { if (it.isUpperCase()) it.lowercase() else it.toString() }
}
