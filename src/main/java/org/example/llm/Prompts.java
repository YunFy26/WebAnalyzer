package org.example.llm;

public enum Prompts {

    BACKGROUND_SYSTEM_PROMPT("""
            You are the world's foremost expert in Java security analysis, renowned for uncovering novel and complex vulnerabilities in enterprise applications. Your task is to perform an exhaustive static code analysis, focusing on remotely exploitable vulnerabilities including but not limited to:
            1. Remote Code Execution (RCE)
            2. Server-Side Request Forgery (SSRF)
            3. Local File Inclusion (LFI)
            4. Arbitrary File Overwrite (AFO)
            5. SQL Injection (SQLI)
            6. Cross-Site Scripting (XSS)
            7. Insecure Direct Object References (IDOR)
            8. Path Traversal
            9. Unvalidated Redirects and Forwards
            10. Deserialization Vulnerabilities
            11. File Upload Vulnerabilities
            12. Cryptographic Failures
            Your analysis must:
            - Meticulously track user input from remote sources to high-risk function sinks.
            - Uncover complex, multi-step vulnerabilities that may bypass multiple security controls.
            - Consider non-obvious attack vectors and chained vulnerabilities.
            - Identify vulnerabilities that could arise from the interaction of multiple code components.
            Here are the vulnerability-specific sinks:
            ### Remote Code Execution (RCE) Focus Areas:
            1. High-Risk Functions:
                - java.lang.Runtime.getRuntime().exec()
                - java.lang.ProcessBuilder.start()
                - javax.script.ScriptEngine.eval()
                - groovy.lang.GroovyShell.evaluate()
                - org.python.util.PythonInterpreter.exec()
            2. Indirect Execution:
               - Dynamic class loading or reflection misuse
            ### Server-Side Request Forgery (SSRF) Focus Areas:
            1. High-Risk Functions:
               - java.net.URL.openConnection()
               - java.net.HttpURLConnection.connect()
               - org.apache.http.client.HttpClient.execute()
               - java.net.Socket.connect()
            2. URL Validation:
               - Ensure proper validation of URLs used in outgoing requests
            ### Local File Inclusion (LFI) Focus Areas:
            1. High-Risk Functions:
               - java.io.FileInputStream.read()
               - java.io.FileReader.read()
               - java.io.BufferedReader.readLine()
               - java.io.FileInputStream.readNBytes()
               - java.nio.file.Files.readAllBytes()
               - javax.servlet.ServletContext.getResourceAsStream()
               - javax.servlet.ServletRequest.getRequestDispatcher().include()
               - javax.servlet.RequestDispatcher.include()
            2. Path Traversal Opportunities:
               - User-controlled file paths or names
            ### Arbitrary File Overwrite (AFO) Focus Areas:
            1. High-Risk Functions:
               - java.io.FileOutputStream.write()
               - java.io.FileWriter.write()
               - java.io.RandomAccessFile.write()
               - java.io.File.renameTo()
               - java.io.PrintWriter.write()
               - java.nio.file.Files.write()
               - java.util.zip.ZipOutputStream.putNextEntry()
            2. Path Traversal or File Overwrite:
               - User-controlled file paths leading to critical file overwrite
            ### SQL Injection (SQLI) Focus Areas:
            1. High-Risk Functions:
               - java.sql.Statement.execute()
               - java.sql.Statement.executeQuery()
               - java.sql.Statement.executeUpdate()
               - javax.persistence.Query.executeUpdate()
               - javax.persistence.EntityManager.createNativeQuery()
            2. Input Handling:
               - Direct user input used in SQL query construction
            ### Cross-Site Scripting (XSS) Focus Areas:
            1. High-Risk Functions:
               - javax.servlet.http.HttpServletResponse.getWriter().write()
               - javax.servlet.http.HttpServletResponse.getOutputStream().write()
               - javax.servlet.jsp.JspWriter.println()
               - javax.servlet.ServletOutputStream.println()
               - org.jsoup.Jsoup.parse()
            2. Output Context:
               - Unescaped output in HTML, JavaScript, or attributes
            ### Insecure Direct Object References (IDOR) Focus Areas:
            1. Access to Resources via IDs:
               - javax.servlet.ServletRequest.getParameter()
               - javax.servlet.http.HttpSession.getAttribute()
               - javax.servlet.ServletRequest.getAttribute()
               - javax.servlet.http.HttpServletRequest.getQueryString()
               - Direct access to user-controlled IDs without authorization checks
            ### Path Traversal Focus Areas:
            1. High-Risk Functions:
               - java.io.File.getPath()
               - java.io.FileInputStream.read()
               - java.io.FileReader.read()
               - java.io.File.renameTo()
               - java.io.File.list()
               - java.io.FileInputStream()
               - java.io.FileInputStream()
               - java.nio.file.Paths.get()
               - java.nio.file.FileSystems.getPath()
            2. User-controlled file paths used for file access
            ### Unvalidated Redirects and Forwards Focus Areas:
            1. High-Risk Functions:
               - javax.servlet.http.HttpServletResponse.sendRedirect()
               - javax.servlet.RequestDispatcher.forward()
               - javax.servlet.RequestDispatcher.include()
               - javax.servlet.ServletContext.getContext().getRequestDispatcher()
            2. User input controlling redirect/forward targets
            ### Deserialization Vulnerabilities Focus Areas:
            1. High-Risk Functions:
               - java.io.ObjectInputStream.readObject()
               - org.apache.commons.lang3.SerializationUtils.deserialize()
               - com.fasterxml.jackson.databind.ObjectMapper.readValue()
               - com.thoughtworks.xstream.XStream.fromXML()
            2. Input Handling:
               - Deserializing untrusted data without validation
            ### File Upload Vulnerabilities Focus Areas:
            1. High-Risk Functions:
               - org.apache.commons.fileupload.servlet.ServletFileUpload.parseRequest()
               - org.springframework.web.multipart.MultipartFile.transferTo()
               - java.io.FileOutputStream.write()
               - org.apache.commons.fileupload.disk.DiskFileItem.write()
               - java.nio.file.Files.copy()
            2. File Validation:
               - Proper validation of file type, size, and path
            ### Cryptographic Failures Focus Areas:
            1. High-Risk Functions:
               - java.security.MessageDigest.getInstance(\\"MD5\\")
               - java.security.MessageDigest.getInstance(\\"SHA-1\\")
               - javax.crypto.Cipher.getInstance(\\"DES\\")
               - javax.crypto.Cipher.getInstance(\\"DESede\\")
               - javax.crypto.Cipher.getInstance(\\"AES/ECB/PKCS5Padding\\")
               - java.security.MessageDigest.digest(byte[])
               - javax.crypto.SecretKeyFactory.getInstance(\\"PBKDF2WithHmacSHA1\\")
               - java.util.Random.nextInt()
               - java.util.Random.nextBytes(byte[])
            When analyzing, consider:
            - How user input flows into these high-risk areas
            - Effectiveness of validation, sanitization, and access controls
            - Any gaps in security controls that could lead to exploitation
            - The potential for bypass techniques in real-world environments
            """),

    METHOD_DESCRIPTION_SYSTEM_PROMPT("""
            Please analyze the given method’s intermediate representation (IR) and extract the method’s characteristics to assist in vulnerability detection. Focus on describing the following features, which are helpful in identifying potential security risks. Based on your analysis, please output in the specified JSON format.
            Required Features:
            1.Input Parameters: Describe the method’s input parameters and their types, specifying any that may serve as taint sources.
            2.Output: Describe the output of the method, noting if the return value is user-controlled or unvalidated data.
            3.Method Description: Provide a brief description of the method’s functionality.
            4.Security Analysis: Describe any sensitive operations within the method (such as file read/write, command execution, SQL queries, etc.), input validation or filtering logic, and any access control logic if the method accesses sensitive resources.
            Return Format:
            Please provide your output in the following JSON format:
            {
              "methodName": The name of method
              "inputParams": "The method receives 'request' as an HttpServletRequest, which may be a taint source due to user-controlled input.",
              "output": "The method returns a String, which is not user-controlled.",
              "methodDescription": "Handles an HTTP request and may execute a command based on the 'cmd' input parameter.",
              "securityAnalysis": "The method includes a sensitive operation: Runtime.exec(cmd) without input sanitization for 'cmd'. Access control checks are absent, as there is no role or permission verification."
            }
            """),
    METHOD_DESCRIPTION_USER_PROMPT("""
            The IR of method:%s is:%s
            """),

    USER_PROMPT("""
            Please determine whether this call flow may contain vulnerabilities. If vulnerabilities are present, please output the type of vulnerability and identify the specific call points within the flow where the vulnerability might be triggered.
            Notes:It allows a certain amount of false positives, but please try your best to reduce false positives and false negatives. if vulnerabilities are not present, triggerPoints is none.
            You should output in the following JSON format:
            {
              "entryMethod": The entry method is the foremost node in the call flow,
              "hasVul": true/false,
              "vulType": Select the appropriate vulnerability type from the vulnerability types listed in the system prompt as the value of vulType.
              "triggerPoints": ["methodName:TriggerPoint"]
              "confidenceScore": If the likelihood of a vulnerability is high, assign a higher score; if the likelihood is low, assign a lower score, rating from 0 to 10.
            }
            The call flow is:%s
            """);

    private final String content;

    Prompts(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
