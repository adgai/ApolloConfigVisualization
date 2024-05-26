package com.adgainai.apolloconfigvisualization

import com.adgainai.apolloconfigvisualization.config.ApolloViewConfiguration
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.impl.FoldRegionImpl
import com.intellij.openapi.editor.impl.FoldingModelImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.keyFMap.KeyFMap
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference


class FileOpenListener : FileEditorManagerListener {

    private val myFoldingInfoInDocumentKey: Key<Any> = Key.create("FOLDING_INFO_IN_DOCUMENT_KEY")

    private val foldingBuilder: MyFoldingBuilder = MyFoldingBuilder()

    //    override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
//        // 在文件打开时触发构建代码折叠区域的逻辑
//        val project: Project? = source.project
//        project?.let {
//            // 获取文件的 PsiElement 根节点
//            val psiFile: PsiFile? = PsiManager.getInstance(it).findFile(file)
//            psiFile?.let { psiFile ->
//                // 调用代码折叠逻辑
//                buildFoldingDescriptors(psiFile, it)
//            }
//        }
//    }
//
//    // 构建代码折叠区域的描述符的逻辑
//    private fun buildFoldingDescriptors(psiFile: PsiFile, project: Project) {
//        // 获取文件的 Document 对象
//        val document: Document? = PsiDocumentManager.getInstance(project).getDocument(psiFile)
//        document?.let { document ->
//            // 调用代码折叠逻辑
//            val descriptors: Array<FoldingDescriptor> = foldingBuilder.buildFoldRegions(psiFile, document, false)
//
//            // 获取 FoldingModel 并添加描述符
//            val editor = FileEditorManager.getInstance(project).getSelectedTextEditor()
//            val foldingModel: FoldingModelImpl = editor?.foldingModel as FoldingModelImpl
//
//            foldingModel.runBatchFoldingOperation {
//                for (foldRegion in foldingModel.allFoldRegions) {
//                    val text = document.getText(foldRegion.textRange)
//                    if (!text.startsWith("configUtils")){
//                        continue
//                    }
//
//                    psiFile.putUserData(Key.create("editor.BeforeCodeFoldingPass"), false)
//                    val foldRegionImpl = foldRegion as FoldRegionImpl
//                    foldRegionImpl.setExpanded(false)
//                    val m = FoldingModelImpl::class.java.declaredMethods.first{ it.name == "collapseFoldRegion" }
//                    m.trySetAccessible()
//                    m.invoke(foldingModel,foldRegion,false)
//                }
//            }
//        }
//    }
    fun fileOpenedc(source: FileEditorManager, file: VirtualFile) {
        // 在文件打开时触发构建代码折叠区域的逻辑
        val project: Project? = source.project
        project?.let {
            // 获取文件的 PsiElement 根节点
            val psiFile: PsiFile? = PsiManager.getInstance(it).findFile(file)
            psiFile?.let { psiFile ->
                // 调用代码折叠逻辑
                buildFoldingDescriptors(psiFile, it)
            }
        }
    }

    // 构建代码折叠区域的描述符的逻辑
    private fun buildFoldingDescriptors(psiFile: PsiFile, project: Project) {
        // 获取文件的 Document 对象
        val document: Document? = PsiDocumentManager.getInstance(project).getDocument(psiFile)
        document?.let { document ->
            // 调用代码折叠逻辑
            val descriptors: Array<FoldingDescriptor> = foldingBuilder.buildFoldRegions(psiFile, document, false)

            // 获取 FoldingModel 并添加描述符
            val editor = FileEditorManager.getInstance(project).getSelectedTextEditor()
            val foldingModel: FoldingModelImpl = editor?.foldingModel as FoldingModelImpl

            foldingModel.runBatchFoldingOperation {
                for (foldRegion in foldingModel.allFoldRegions) {
                    val text = document.getText(foldRegion.textRange)
                    if (!text.startsWith("configUtils")) {
                        continue
                    }

                    psiFile.putUserData(Key.create("editor.BeforeCodeFoldingPass"), false)
                    val foldRegionImpl = foldRegion as FoldRegionImpl
                    foldRegionImpl.setExpanded(false)
                    val m = FoldingModelImpl::class.java.declaredMethods.first { it.name == "collapseFoldRegion" }
                    m.trySetAccessible()
                    m.invoke(foldingModel, foldRegion, false)
                }
            }
        }
    }

    override fun fileClosed(source: FileEditorManager, file: VirtualFile) {

        val configuration = ApolloViewConfiguration.getInstance(
            source.project
        )

        if (!configuration.foldingWhenEveryOpenFile) {
            return
        }


        super.fileClosed(source, file)
        val document = FileDocumentManager.getInstance().getDocument(file)

        val documentFoldingInfo = document?.getUserData(myFoldingInfoInDocumentKey)


        val f = AtomicReference::class.java.getDeclaredField("value")
        f.trySetAccessible()
        var a = f.get(document) as KeyFMap
//        a.keys.filter { k->k.get }
//        Key::class.java.getDeclaredField("myName")
//        val clearmethod: Method? = documentFoldingInfo?.javaClass?.getDeclaredMethod("clear")

        var n = Key::class.java.getDeclaredField("myName")
        n.trySetAccessible()
        n.get(a.keys.get(0))
        val first = a.keys.filter { k -> n.get(k).equals("FOLDING_INFO_IN_DOCUMENT_KEY") }.first()

        val get = a.get(first)
//        (get as DocumentFoldingInfo).myInfos.clear()


        val fileEditors = FileEditorManager.getInstance(source.project).getEditors(file)

        // 获取当前活动编辑器

        val psiFile: PsiFile = PsiManager.getInstance(source.project).findFile(file)!!

        val descriptors: Array<FoldingDescriptor>? = document?.let {
            psiFile.let { it1 ->
                foldingBuilder.buildFoldRegions(
                    it1,
                    it, false
                )
            }
        }
        if (descriptors == null || descriptors.size <= 0) {
            return
        }

        //e#121#143#0
        val sigSet = descriptors.filter { fr -> fr.placeholderText?.startsWith("Apollo config") ?: false }.map { fr ->
            String.format("e#%d#%d#0", fr.range.startOffset, fr.range.endOffset);
        }.toSet()

        val declaredField = get?.javaClass?.getDeclaredField("myInfos")
        declaredField?.trySetAccessible()
        val anies = declaredField?.get(get) as List<Any>

        val toList = anies.filter { a ->

            val field = a.javaClass.getDeclaredField("signature")
            field.trySetAccessible()
            val s = field.get(a) as String
            sigSet.contains(s)

        }.toList()

        val removeMethod = anies.javaClass.getDeclaredMethod("remove", Object::class.java)
        removeMethod.trySetAccessible()

        toList.forEach { removeMethod.invoke(anies, it) }


//        var m = get?.javaClass?.getDeclaredMethod("clear")
//        m?.trySetAccessible()
//        m?.invoke(get)
    }
}
