package com.sharry.srouter.plugin.base

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.sharry.srouter.plugin.util.Logger
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * 用来处理文件扫描的 Base Transform
 *
 * @author Sharry
 * @since 17/3/21 11:48
 */
abstract class BaseFileScanTransform : Transform() {

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(context: Context?,
                           inputs: MutableCollection<TransformInput>?,
                           referencedInputs: MutableCollection<TransformInput>?,
                           outputProvider: TransformOutputProvider?,
                           isIncremental: Boolean) {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
        Logger.i("|---------------------Transform start---------------------|")
        outputProvider?.deleteAll()
        // 1. 扫描文件
        Logger.i("|---------- Start scan file ----------|")
        var startTime = System.currentTimeMillis()
        val leftSlash = File.separator == "/"
        inputs?.forEach { input ->
            // 1.1 从 jar 包中找寻目标文件
            input.jarInputs.forEach { jarInput ->
                var destName = jarInput.name
                // rename jar files
                val hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length - 4)
                }
                // input file
                val src = jarInput.file
                // output file
                outputProvider?.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)?.let {
                    // 扫描 jar 包, 找寻目标文件
                    if (canScanJarFile(src.absolutePath)) {
                        val jarFile = JarFile(src)
                        val enumeration: Enumeration<*> = jarFile.entries()
                        while (enumeration.hasMoreElements()) {
                            val jarEntry = enumeration.nextElement() as JarEntry
                            onScanJarEntry(src, jarFile, jarEntry)
                        }
                        jarFile.close()
                    }
                    FileUtils.copyFile(src, it)
                }
            }
            // 1.2 从 Class 文件中找寻目标文件
            input.directoryInputs.forEach { directoryInput ->
                var root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                directoryInput.file.eachFileRecurse(FileType.ANY) { file ->
                    var filePath = file.absolutePath.replace(root, "")
                    if (!leftSlash) {
                        filePath = filePath.replace("\\\\", "/")
                    }
                    if (file.isFile && canScanClassFile(filePath)) {
                        onScanClass(file, filePath)
                    }
                }
                // copy to dest
                val dest = outputProvider?.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
        Logger.i("|---------- Scan finish, current cost time " + (System.currentTimeMillis() - startTime) + "ms ----------|")
        // 2. 处理扫描的文件
        startTime = System.currentTimeMillis()
        Logger.i("|---------- Start process ----------|")
        onProcess()
        Logger.i("|---------- Process finish, current cost time " + (System.currentTimeMillis() - startTime) + "ms ----------|")
    }

    /**
     * 扫描 Jar 包中的 JarEntry
     */
    abstract fun onScanJarEntry(file: File, jarFile: JarFile, jarEntry: JarEntry)

    /**
     * 扫描 .class 文件
     */
    abstract fun onScanClass(classFile: File, classFilePath: String)

    abstract fun onProcess()

    private fun canScanJarFile(path: String): Boolean {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    private fun canScanClassFile(filePath: String): Boolean {
        return filePath.endsWith(".class") && !filePath.startsWith("R\$") && "R.class" != filePath && "BuildConfig.class" != filePath
    }

}

@Throws(FileNotFoundException::class, IllegalArgumentException::class)
fun File.eachFileRecurse(fileType: FileType, closure: (file: File) -> Unit) {
    checkDir()
    val files = this.listFiles()
    if (files != null) {
        val var5 = files.size
        for (var6 in 0 until var5) {
            val file = files[var6]
            if (file.isDirectory) {
                if (fileType != FileType.FILES) {
                    closure(file)
                }
                file.eachFileRecurse(fileType, closure)
            } else if (fileType != FileType.DIRECTORIES) {
                closure(file)
            }
        }
    }
}

@Throws(FileNotFoundException::class, java.lang.IllegalArgumentException::class)
fun File.checkDir() {
    if (!exists()) {
        throw FileNotFoundException(absolutePath)
    } else if (!isDirectory) {
        throw java.lang.IllegalArgumentException("The provided File object is not a directory: $absolutePath")
    }
}
