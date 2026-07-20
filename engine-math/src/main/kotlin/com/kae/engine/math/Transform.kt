package com.kae.engine.math

/**
 * A 3D transform component combining position, rotation, and scale.
 * The resulting transformation matrix is computed as: Translation * Rotation * Scale.
 */
data class Transform(
    var position: Vec3 = Vec3.ZERO,
    var rotation: Quaternion = Quaternion.IDENTITY,
    var scale: Vec3 = Vec3.ONE
) {

    /**
     * Computes the 4x4 transformation matrix for this transform.
     * The transformation order is: Scale → Rotate → Translate.
     */
    fun toMatrix(): Mat4 {
        val r = rotation.toMat4()
        val t = Mat4.translation(position)
        val s = Mat4.scale(scale)
        return t * r * s
    }

    /**
     * Returns the forward direction vector (negative Z axis) of this transform in world space.
     */
    fun forward(): Vec3 = rotation * Vec3.FORWARD

    /**
     * Returns the right direction vector (positive X axis) of this transform in world space.
     */
    fun right(): Vec3 = rotation * Vec3.RIGHT

    /**
     * Returns the up direction vector (positive Y axis) of this transform in world space.
     */
    fun up(): Vec3 = rotation * Vec3.UP

    /**
     * Translates this transform by [delta] in local space (rotated by the current rotation).
     */
    fun translate(delta: Vec3) {
        position = position + rotation * delta
    }

    /**
     * Rotates this transform around the given [axis] by [radians] radians.
     * The rotation is applied in local space.
     */
    fun rotate(axis: Vec3, radians: Float) {
        rotation = (Quaternion.fromAxisAngle(axis, radians) * rotation).normalized()
    }

    /**
     * Scales this transform by the given [factor].
     */
    fun scaleBy(factor: Vec3) {
        scale = Vec3(scale.x * factor.x, scale.y * factor.y, scale.z * factor.z)
    }

    /**
     * Combines this transform with [other]. The result applies [other] first, then this transform.
     * Equivalent to: this.toMatrix() * other.toMatrix().
     */
    operator fun times(other: Transform): Transform {
        val combinedMatrix = this.toMatrix() * other.toMatrix()
        return Transform.fromMatrix(combinedMatrix)
    }

    /**
     * Decomposes a 4x4 matrix into a Transform (position, rotation, scale).
     * Assumes the matrix is a valid TRS (Translation-Rotation-Scale) matrix.
     */
    companion object {
        /**
         * Creates a Transform from a position, rotation, and scale.
         */
        fun fromPositionRotationScale(position: Vec3, rotation: Quaternion, scale: Vec3): Transform =
            Transform(position, rotation, scale)

        /**
         * Decomposes a 4x4 matrix into a Transform (position, rotation, scale).
         * Assumes the matrix is a valid TRS (Translation-Rotation-Scale) matrix.
         */
        fun fromMatrix(matrix: Mat4): Transform {
            val position = matrix.getTranslation()
            val scale = matrix.getScale()
            val rotationMat = matrix.getRotation()

            val m00 = rotationMat[0, 0]
            val m01 = rotationMat[0, 1]
            val m02 = rotationMat[0, 2]
            val m10 = rotationMat[1, 0]
            val m11 = rotationMat[1, 1]
            val m12 = rotationMat[1, 2]
            val m20 = rotationMat[2, 0]
            val m21 = rotationMat[2, 1]
            val m22 = rotationMat[2, 2]

            val trace = m00 + m11 + m22
            val rotation = if (trace > 0f) {
                val s = 0.5f / kotlin.math.sqrt(trace + 1f)
                Quaternion(
                    (m21 - m12) * s,
                    (m02 - m20) * s,
                    (m10 - m01) * s,
                    0.25f / s
                )
            } else if (m00 > m11 && m00 > m22) {
                val s = 2f * kotlin.math.sqrt(1f + m00 - m11 - m22)
                Quaternion(
                    0.25f * s,
                    (m01 + m10) / s,
                    (m02 + m20) / s,
                    (m21 - m12) / s
                )
            } else if (m11 > m22) {
                val s = 2f * kotlin.math.sqrt(1f + m11 - m00 - m22)
                Quaternion(
                    (m01 + m10) / s,
                    0.25f * s,
                    (m12 + m21) / s,
                    (m02 - m20) / s
                )
            } else {
                val s = 2f * kotlin.math.sqrt(1f + m22 - m00 - m11)
                Quaternion(
                    (m02 + m20) / s,
                    (m12 + m21) / s,
                    0.25f * s,
                    (m10 - m01) / s
                )
            }

            return Transform(position, rotation, scale)
        }
    }
}
