package com.mongodbmodfactory.gradle

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.VariableDeclarator
import com.github.javaparser.ast.expr.MemberValuePair
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.github.javaparser.ast.stmt.ReturnStmt
import com.github.javaparser.ast.type.ClassOrInterfaceType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class TransformControllersTask extends DefaultTask {
    @Input
    String rootPackage

    @InputDirectory
    File sourceDir

    @OutputDirectory
    File outputDir

    @TaskAction
    void transform() {
        sourceDir.eachDirRecurse { dir ->
            dir.eachFile { file ->
                if (file.name.endsWith('.java')) {
                    transformFile(file)
                }
            }
        }
    }

    void transformFile(File file) {
        def cu = JavaParser.parse(file)
        
        // Transform package
        def packageName = cu.packageDeclaration.get().nameAsString
        def newPackageName = packageName.replace('<root-package>', rootPackage)
        cu.packageDeclaration.get().name = newPackageName

        // Transform class
        def classDecl = cu.findFirst(ClassOrInterfaceDeclaration).get()
        
        // Replace @Model with @Controller
        classDecl.annotations.removeIf { it.nameAsString == 'Model' }
        classDecl.addAnnotation('Controller')
        
        // Replace CDI annotations with Spring annotations
        classDecl.annotations.each { annotation ->
            switch (annotation.nameAsString) {
                case 'Inject':
                    annotation.name = 'Autowired'
                    break
                case 'Named':
                    annotation.name = 'ModelAttribute'
                    break
            }
        }

        // Transform methods
        classDecl.methods.each { method ->
            switch (method.nameAsString) {
                case 'register':
                    transformRegisterMethod(method)
                    break
                case 'initNewMember':
                    transformInitMethod(method)
                    break
            }
        }

        // Add Spring-specific fields
        addSpringFields(classDecl)

        // Write transformed file
        def outputFile = new File(outputDir, file.name)
        outputFile.parentFile.mkdirs()
        outputFile.text = cu.toString()
    }

    private void transformRegisterMethod(MethodDeclaration method) {
        // Add @PostMapping
        method.addAnnotation(new NormalAnnotationExpr(
            new NameExpr('PostMapping'),
            [new MemberValuePair('value', new StringLiteralExpr('/register'))]
        ))

        // Transform method body to use Spring's Model and RedirectAttributes
        def block = new BlockStmt()
        block.addStatement(new ExpressionStmt(new MethodCallExpr(
            new NameExpr('model'),
            'addAttribute',
            [new StringLiteralExpr('message'), new StringLiteralExpr('Registered!')]
        )))
        block.addStatement(new ReturnStmt(new StringLiteralExpr('redirect:/')))
        method.body = block
    }

    private void transformInitMethod(MethodDeclaration method) {
        // Add @ModelAttribute
        method.addAnnotation(new NormalAnnotationExpr(
            new NameExpr('ModelAttribute'),
            [new MemberValuePair('value', new StringLiteralExpr('member'))]
        ))
    }

    private void addSpringFields(ClassOrInterfaceDeclaration classDecl) {
        // Add Model field
        def modelField = new FieldDeclaration()
        modelField.addVariable(new VariableDeclarator(
            new ClassOrInterfaceType('Model'),
            'model'
        ))
        modelField.addAnnotation('Autowired')
        classDecl.addMember(modelField)

        // Add RedirectAttributes field
        def redirectField = new FieldDeclaration()
        redirectField.addVariable(new VariableDeclarator(
            new ClassOrInterfaceType('RedirectAttributes'),
            'redirectAttributes'
        ))
        redirectField.addAnnotation('Autowired')
        classDecl.addMember(redirectField)
    }
} 