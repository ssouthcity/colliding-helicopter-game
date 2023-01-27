package dev.southcity.collidingcopters

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.ScreenUtils

class CollidingCoptersGame : ApplicationAdapter() {
    companion object {
        const val SCREEN_WIDTH = 1080f
        const val SCREEN_HEIGHT = 920f
        const val SPAWN_INTERVAL = 5f
    }

    private lateinit var batch: SpriteBatch
    private lateinit var camera: OrthographicCamera
    private lateinit var copters: ArrayList<Helicopter>
    private var spawnTimer: Float = 0f

    override fun create() {
        batch = SpriteBatch()

        camera = OrthographicCamera()
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)

        copters = ArrayList()

        spawnTimer = SPAWN_INTERVAL
    }

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime

        spawnTimer += deltaTime
        while (spawnTimer >= SPAWN_INTERVAL) {
            copters.add(Helicopter())
            spawnTimer -= SPAWN_INTERVAL
        }

        for (copter in copters) {
            copter.update(deltaTime)

            for (other in copters) {
                if (copter.collides(other)) {
                    val tmpVel = copter.velocity;
                    copter.velocity = other.velocity;
                    other.velocity = tmpVel;
                }
            }
        }

        batch.projectionMatrix = camera.combined

        ScreenUtils.clear(0f, 0f, 0f, 1f)

        batch.begin()
        for (copter in copters) {
            copter.draw(batch)
        }
        batch.end()
    }

    override fun dispose() {
        batch.dispose()

        for (copter in copters) {
            copter.dispose()
        }
    }
}