package dev.southcity.collidingcopters

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Disposable
import kotlin.random.Random

class Helicopter : Disposable {
    companion object {
        const val SPEED: Float = 1000f
    }

    private val animation: Animation<Texture> = Animation(0.1f,
        Texture("heli1.png"),
        Texture("heli2.png"),
        Texture("heli3.png"),
        Texture("heli4.png"),
    )

    private var position: Vector2 = Vector2(
        CollidingCoptersGame.SCREEN_WIDTH / 2f - currentFrame.width / 2f,
        CollidingCoptersGame.SCREEN_HEIGHT / 2f - currentFrame.height / 2f
    )

    var velocity: Vector2 = Vector2(
        Random.nextFloat() - 0.5f,
        Random.nextFloat() - 0.5f,
    ).setLength(SPEED)

    private var accumulatedTime: Float = 0f

    private val currentFrame: Texture
        get() = animation.getKeyFrame(accumulatedTime, true)

    private val collisionShape: Polygon = Polygon(floatArrayOf(
                0f, 0f,
                currentFrame.width.toFloat(), 0f,
                currentFrame.width.toFloat(), currentFrame.height.toFloat(),
                0f, currentFrame.height.toFloat()))

    fun collides(other: Helicopter): Boolean {
        return Intersector.overlapConvexPolygons(collisionShape, other.collisionShape)
    }

    fun update(delta: Float) {
        accumulatedTime += Gdx.graphics.deltaTime
        accumulatedTime %= animation.animationDuration

        position.x += velocity.x * delta
        position.y += velocity.y * delta

        if (position.x < 0 || position.x + currentFrame.width > CollidingCoptersGame.SCREEN_WIDTH) {
            position.x = position.x.coerceIn(0f, CollidingCoptersGame.SCREEN_WIDTH - currentFrame.width)
            velocity.x *= -1
        }

        if (position.y < 0 || position.y + currentFrame.height > CollidingCoptersGame.SCREEN_HEIGHT) {
            position.y = position.y.coerceIn(0f, CollidingCoptersGame.SCREEN_HEIGHT - currentFrame.height)
            velocity.y *= -1
        }

        collisionShape.setPosition(position.x, position.y)
        collisionShape.setOrigin(currentFrame.width / 2f, currentFrame.height / 2f)
        collisionShape.rotation = velocity.angleDeg() - 180
    }

    fun draw(batch: SpriteBatch) {
        batch.draw(
            currentFrame,
            position.x, position.y, // coordinates
            currentFrame.width / 2f, currentFrame.height / 2f, // origin for scale and rotation
            currentFrame.width.toFloat(), currentFrame.height.toFloat(), // dimensions
            1f, 1f, // scaling
            velocity.angleDeg() - 180, // rotation
            0, 0, // source rect bottom left
            currentFrame.width, currentFrame.height, // source rect dimensions
            false, false // flips
        )
    }

    override fun dispose() {
        for (frame in animation.keyFrames) {
            frame.dispose()
        }
    }
}

