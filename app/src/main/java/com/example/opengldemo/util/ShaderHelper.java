package com.example.opengldemo.util;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderHelper {
    private final static String TAG = ShaderHelper.class.getSimpleName();

    /**
     * compile VertexShader
     *
     * @param shaderCode the shaderCode
     * @return the vertex shaderId
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * compile the Fragment Shader
     *
     * @param shaderCode the Fragment Shader code
     * @return the fragment shaderId
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        final int shaderId = GLES20.glCreateShader(type);
        if (shaderId != 0) {
            GLES20.glShaderSource(shaderId, shaderCode);
            GLES20.glCompileShader(shaderId);
            int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(shaderId);
                Log.w(TAG, "Compilation of shader failed.");
                return 0;
            }
        }
        return shaderId;
    }

    /**
     *
     * @param vertexShaderId
     * @param fragmentShaderId
     * @return
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programId = GLES20.glCreateProgram();
        if (programId != 0) {
            GLES20.glAttachShader(programId, vertexShaderId);
            GLES20.glAttachShader(programId, fragmentShaderId);
            GLES20.glLinkProgram(programId);
            int[] programStatus = new int[1];
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, programStatus, 0);
            if (programStatus[0] == 0) {
                GLES20.glDeleteProgram(programId);
                Log.w(TAG, "link of program failed. " + GLES20.glGetProgramInfoLog(programId));
                return 0;
            }
        }
        return programId;
    }

    /**
     * create the linked program id with shaders
     *
     * @param vertexShaderCode the vertexShaderCode
     * @param fragmentShaderCode the fragmentShaderCode
     * @return the linked program id
     */
    public static int createProgram(String vertexShaderCode, String fragmentShaderCode) {
        return linkProgram(compileVertexShader(vertexShaderCode),
                compileFragmentShader(fragmentShaderCode));
    }

}
