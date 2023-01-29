package dev.southcity.collidingcopters

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.random.Random

class CollidingCoptersGame : ApplicationAdapter() {
    companion object {
        const val SCREEN_WIDTH = 80f
        const val SCREEN_HEIGHT = SCREEN_WIDTH * 9 / 16
        const val SPAWN_INTERVAL = 5f
    }

    private lateinit var batch: SpriteBatch
    private lateinit var debugRenderer: Box2DDebugRenderer
    private lateinit var camera: OrthographicCamera
    private lateinit var world: World
    private lateinit var copters: ArrayList<Body>
    private lateinit var copterAnimation: Animation<Texture>
    private var spawnTimer: Float = 0f
    private var animationTimer: Float = 0f

    private var helicopterWidth: Float = 10f
    private var helicopterHeight: Float = 0f

    override fun create() {
        batch = SpriteBatch()

        debugRenderer = Box2DDebugRenderer()

        camera = OrthographicCamera()
        camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT)

        world = World(Vector2.Zero, true)

        copters = ArrayList()

        copterAnimation = Animation(0.1f,
            Texture("heli1.png"),
            Texture("heli2.png"),
            Texture("heli3.png"),
            Texture("heli4.png"),
        )

        spawnTimer = SPAWN_INTERVAL

        helicopterHeight = helicopterWidth *
                copterAnimation.keyFrames[0].height.toFloat() /
                copterAnimation.keyFrames[0].width.toFloat()

        spawnScreenBounds()
    }

    fun spawnScreenBounds() {
        var bDef = BodyDef().apply {
            type = BodyType.StaticBody
            position.set(0f, 0f)
        }

        val body = world.createBody(bDef)

        val collisionShape = EdgeShape()
        // bottom
        collisionShape.set(0f, 0f, SCREEN_WIDTH, 0f)
        body.createFixture(collisionShape, 0f)
        // top
        collisionShape.set(0f, SCREEN_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT)
        body.createFixture(collisionShape, 0f)
        // left
        collisionShape.set(0f, 0f, 0f, SCREEN_HEIGHT)
        body.createFixture(collisionShape, 0f)
        // right
        collisionShape.set(SCREEN_WIDTH, 0f, SCREEN_WIDTH, SCREEN_HEIGHT)
        body.createFixture(collisionShape, 0f)

        collisionShape.dispose()
    }

    fun spawnHelicopter(): Body {
        val bDef = BodyDef().apply {
            type = BodyType.DynamicBody
            position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f)
            linearVelocity.set(Random.nextFloat() - 0.5f, Random.nextFloat() - 0.5f)
            linearVelocity.setLength(10f)
        }

        val body = world.createBody(bDef)

        val collisionShape = PolygonShape()
        collisionShape.setAsBox(
             helicopterWidth / 2f,
            helicopterHeight / 2f
        )

        val fixtureDef = FixtureDef().apply {
            density = 0f
            friction = 0f
            restitution = 1f
            shape = collisionShape
        }

        body.createFixture(fixtureDef)

        collisionShape.dispose()

        return body
    }

    override fun render() {
        val deltaTime = Gdx.graphics.deltaTime

        animationTimer += deltaTime
        spawnTimer += deltaTime

        while (spawnTimer >= SPAWN_INTERVAL) {
            copters.add(spawnHelicopter())
            spawnTimer -= SPAWN_INTERVAL
        }

        animationTimer %= copterAnimation.animationDuration * copterAnimation.keyFrames.size

        world.step(deltaTime, 6, 2)

        ScreenUtils.clear(0f, 0f, 0f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()
        for (copter in copters) {
            copter.setTransform(copter.position, copter.linearVelocity.angleRad())

            val frame = copterAnimation.getKeyFrame(animationTimer, true)
            batch.draw(
                frame,
                copter.position.x - helicopterWidth / 2,
                copter.position.y - helicopterHeight / 2,
                helicopterWidth / 2f, helicopterHeight / 2f,
                helicopterWidth, helicopterHeight,
                1f, 1f,
                copter.linearVelocity.angleDeg() - 180f,
                0, 0,
                frame.width, frame.height,
                false, false
            )
        }
        batch.end()

        debugRenderer.render(world, camera.combined)
    }

    override fun dispose() {
        batch.dispose()
        world.dispose()

        for (keyframe in copterAnimation.keyFrames) {
            keyframe.dispose()
        }
    }
}