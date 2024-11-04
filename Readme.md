# Unit Test Generator Script

This Kotlin script automates the generation template for unit tests based on annotated functions in a specified Kotlin source file. It scans the file for functions marked with a specified annotation and creates corresponding test functions in a designated test directory.

## Features

- Scans a Kotlin source file for functions annotated with a specified annotation.
- Generates unit test functions with placeholders for test logic.
- Creates a new test file in the appropriate test directory based on the source file's location.

## Prerequisites

- Kotlin 1.3 or higher
- JUnit 4 or higher for unit testing
- A Gradle build system

## How It Works

The script reads a specified Kotlin source file, looking for functions that have been annotated with a specific annotation (e.g., `@GenerateTest`). For each annotated function, it generates a corresponding test function in a new test file. This automation reduces the manual effort involved in writing boilerplate test code.

## Download
Download [script](https://github.com/raystatic/UnitTestGenerator/blob/main/utgen.kts) from the GitHub repo (``utgen.kts``) and save it in an accessible location on your machine

![image](https://s6.ezgif.com/tmp/ezgif-6-1240ecee46.gif)

## Usage
Create a custom annotation (e.g; ``GenerateTest``)
```kotlin
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class GenerateTest
```
Add created annotation to the functions for which unit tests need to be created
```kotlin
@GenerateTest
fun add(a: Int, b: Int): Int {
    return a + b
}
```

Run the script, using following command in terminal

```bash
kotlin utgen.kt <annotation_name> <path_to_class>
```
- **annotation_name**: Created annotation (e.g; GenerateTest)
- **path_to_class**: Absolute path to the file for which unit test functions need to be created<img width="581" alt="Screenshot 2024-11-04 at 5 11 24 PM" src="https://github.com/user-attachments/assets/1d7e8f1c-8219-4653-aec4-70c9556edbc9">

## Example of Generated Test

For the following annotated function:

```kotlin
/**
 * Unit tests for CalculatorTest.kt, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class CalculatorTest {
    @Before
    fun setup() {
        //TODO: setup prerequisites for tests
    }

    @After
    fun tearDown() {
        //TODO: clear resources after test
    }

    @Test
    fun addTest() {
        //TODO: val result = add(a, b)
        //TODO: Add logic for your test
        //TODO: Assert the result is expected
    }
}
```

## Test file location
The test file will be created in the test directory in the appropriate package structure
<img width="581" alt="Screenshot 2024-11-04 at 4 59 04 PM" src="https://github.com/user-attachments/assets/93460b75-8935-4385-9ed7-94b6db7110c8">

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
