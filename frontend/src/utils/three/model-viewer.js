import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'

/**
 * 3D模型查看器类
 * 用于展品详情页面的模型展示，支持缩放、旋转
 */
export class ModelViewer {
  constructor(options = {}) {
    this.container = options.container
    this.onProgress = options.onProgress || (() => {})
    this.onComplete = options.onComplete || (() => {})
    this.onError = options.onError || (() => {})

    this.scene = null
    this.camera = null
    this.renderer = null
    this.controls = null
    this.model = null
    this.animationId = null
    
    // 自动旋转
    this.autoRotate = options.autoRotate !== false
    this.autoRotateSpeed = options.autoRotateSpeed || 1
  }

  /**
   * 初始化查看器
   */
  async init() {
    this.createScene()
    this.createCamera()
    this.createRenderer()
    this.createLights()
    this.createControls()
    this.bindEvents()
    this.animate()
  }

  /**
   * 创建场景
   */
  createScene() {
    this.scene = new THREE.Scene()
    // 暖深色背景，与展品详情页色系统一
    this.scene.background = new THREE.Color(0x100e0b)
  }

  /**
   * 创建相机
   */
  createCamera() {
    const aspect = this.container.clientWidth / this.container.clientHeight
    this.camera = new THREE.PerspectiveCamera(45, aspect, 0.1, 1000)
    this.camera.position.set(0, 1, 3)
  }

  /**
   * 创建渲染器
   */
  createRenderer() {
    this.renderer = new THREE.WebGLRenderer({
      antialias: true,
      alpha: true
    })
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.outputColorSpace = THREE.SRGBColorSpace
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping
    this.renderer.toneMappingExposure = 1.2
    this.container.appendChild(this.renderer.domElement)
  }

  /**
   * 创建灯光
   */
  createLights() {
    // 环境光
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8)
    this.scene.add(ambientLight)

    // 主光源
    const mainLight = new THREE.DirectionalLight(0xffffff, 1.2)
    mainLight.position.set(5, 10, 7)
    this.scene.add(mainLight)

    // 补光
    const fillLight = new THREE.DirectionalLight(0xffffff, 0.6)
    fillLight.position.set(-5, 5, -5)
    this.scene.add(fillLight)

    // 底部补光
    const bottomLight = new THREE.DirectionalLight(0xffffff, 0.3)
    bottomLight.position.set(0, -5, 0)
    this.scene.add(bottomLight)
  }

  /**
   * 创建控制器
   */
  createControls() {
    this.controls = new OrbitControls(this.camera, this.renderer.domElement)
    this.controls.enableDamping = true
    this.controls.dampingFactor = 0.05
    this.controls.enableZoom = true
    this.controls.enablePan = false
    this.controls.minDistance = 0.5
    this.controls.maxDistance = 10
    this.controls.autoRotate = this.autoRotate
    this.controls.autoRotateSpeed = this.autoRotateSpeed
  }

  /**
   * 加载模型
   * @param {string} modelPath 模型路径
   */
  async loadModel(modelPath) {
    const loader = new GLTFLoader()
    
    // 配置DRACO解码器
    const dracoLoader = new DRACOLoader()
    dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.6/')
    loader.setDRACOLoader(dracoLoader)

    try {
      const gltf = await new Promise((resolve, reject) => {
        loader.load(
          modelPath,
          resolve,
          (xhr) => {
            if (xhr.lengthComputable) {
              const progress = (xhr.loaded / xhr.total) * 100
              this.onProgress(progress)
            }
          },
          reject
        )
      })

      this.model = gltf.scene
      
      // 先计算原始边界
      const box = new THREE.Box3().setFromObject(this.model)
      const size = box.getSize(new THREE.Vector3())
      const maxDim = Math.max(size.x, size.y, size.z)
      
      // 先缩放到统一大小
      const targetSize = 2.0
      if (maxDim > 0) {
        const scale = targetSize / maxDim
        this.model.scale.setScalar(scale)
      }
      
      // 缩放后重新计算边界并居中
      const scaledBox = new THREE.Box3().setFromObject(this.model)
      const scaledCenter = scaledBox.getCenter(new THREE.Vector3())
      this.model.position.sub(scaledCenter)
      
      // 相机以固定合适距离观察归一化后的模型
      const cameraDistance = targetSize * 2.2
      this.camera.position.set(0, targetSize * 0.3, cameraDistance)
      this.controls.target.set(0, 0, 0)
      this.controls.update()
      
      this.scene.add(this.model)
      this.onComplete()
      
    } catch (error) {
      console.error('模型加载失败:', error)
      this.onError(error)
    }
  }

  /**
   * 绑定事件
   */
  bindEvents() {
    window.addEventListener('resize', this.onWindowResize.bind(this))
  }

  /**
   * 窗口大小变化处理
   */
  onWindowResize() {
    if (!this.container) return
    
    const width = this.container.clientWidth
    const height = this.container.clientHeight

    this.camera.aspect = width / height
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(width, height)
  }

  /**
   * 动画循环
   */
  animate() {
    this.animationId = requestAnimationFrame(this.animate.bind(this))
    
    if (this.controls) {
      this.controls.update()
    }
    
    if (this.renderer && this.scene && this.camera) {
      this.renderer.render(this.scene, this.camera)
    }
  }

  /**
   * 设置自动旋转
   */
  setAutoRotate(enabled) {
    this.autoRotate = enabled
    if (this.controls) {
      this.controls.autoRotate = enabled
    }
  }

  /**
   * 重置视角
   */
  resetView() {
    if (!this.model) return
    
    const targetSize = 2.0
    const cameraDistance = targetSize * 2.2
    this.camera.position.set(0, targetSize * 0.3, cameraDistance)
    this.controls.target.set(0, 0, 0)
    this.controls.update()
  }

  /**
   * 销毁查看器
   */
  dispose() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
    }

    window.removeEventListener('resize', this.onWindowResize.bind(this))

    if (this.controls) {
      this.controls.dispose()
    }

    if (this.renderer) {
      this.renderer.dispose()
      this.container.removeChild(this.renderer.domElement)
    }

    // 清理场景
    if (this.scene) {
      this.scene.traverse((object) => {
        if (object.geometry) {
          object.geometry.dispose()
        }
        if (object.material) {
          if (Array.isArray(object.material)) {
            object.material.forEach(material => material.dispose())
          } else {
            object.material.dispose()
          }
        }
      })
    }
  }
}
