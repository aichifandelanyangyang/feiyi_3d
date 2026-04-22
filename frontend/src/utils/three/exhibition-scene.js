import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { PointerLockControls } from 'three/examples/jsm/controls/PointerLockControls.js'

/**
 * 展厅场景管理类
 * 负责3D展厅的初始化、渲染、交互控制
 */
export class ExhibitionScene {
  constructor(options) {
    // 容器元素
    this.container = options.container
    this.minimapCanvas = options.minimapCanvas
    // 回调函数
    this.onProgress = options.onProgress || (() => {})
    this.onComplete = options.onComplete || (() => {})
    this.onObjectClick = options.onObjectClick || (() => {})  // 物体点击回调
    
    // Three.js核心对象
    this.scene = null
    this.camera = null
    this.renderer = null
    this.controls = null
    this.orbitControls = null
    this.pointerLockControls = null
    
    // 展厅模型
    this.hallModel = null
    
    // 天花板（第一人称模式显示，自由视角隐藏）
    this.ceiling = null
    
    // 控制状态
    this.isWalkMode = true
    this.moveSpeed = 5
    this.clock = new THREE.Clock()
    
    // 移动状态
    this.moveState = {
      forward: false,
      backward: false,
      left: false,
      right: false
    }
    
    // 碰撞检测
    this.raycaster = new THREE.Raycaster()
    this.collisionObjects = []
    
    // 点击交互
    this.clickRaycaster = new THREE.Raycaster()
    this.mouse = new THREE.Vector2()
    this.clickableObjects = []  // 可点击的物体列表
    this.modelStructure = []    // 模型结构信息
    
    // 地面位置指示器（蓝色光圈）
    this.positionIndicator = null
    this.floorObjects = []  // 地面物体列表，用于检测点击地面
    
    // 展品悬停高亮
    this.hoveredExhibit = null  // 当前悬停的展品
    this.originalMaterials = new Map()  // 保存原始材质
    
    // 展品悬停回调（用于显示提示）
    this.onExhibitHover = null
    
    // 视频悬停
    this.hoveredVideo = null
    this.onVideoHover = null
    
    // 展品标记
    this.exhibitMarkers = []  // 展品标记列表
    this.exhibitGroups = []   // 展品组列表
    
    // 点击移动
    this.isMovingToTarget = false  // 是否正在移动到目标
    this.moveTarget = null         // 移动目标位置
    this.moveSpeed = 5             // 移动速度
    
    // 初始相机位置（将根据模型自动调整）
    this.initialCameraPosition = new THREE.Vector3(0, 2, 5)
    this.initialCameraTarget = new THREE.Vector3(0, 1.5, 0)
    
    // 动画ID
    this.animationId = null
    
    // 小地图
    this.minimapContext = null
    
    // 视频屏幕（支持多个）
    this.videoScreens = []  // { element, texture, screen, plane, name }
  }

  /**
   * 初始化场景
   */
  async init() {
    this.onProgress(5, '创建渲染器...')
    this.createRenderer()
    
    this.onProgress(10, '创建场景...')
    this.createScene()
    
    this.onProgress(15, '创建相机...')
    this.createCamera()
    
    this.onProgress(20, '创建灯光...')
    this.createLights()
    
    this.onProgress(25, '初始化控制器...')
    this.createControls()
    
    this.onProgress(30, '加载展厅模型...')
    await this.loadHallModel()
    
    this.onProgress(95, '初始化小地图...')
    this.initMinimap()
    
    this.onProgress(100, '加载完成')
    this.bindEvents()
    this.animate()
    this.onComplete()
    
    // 自动进入漫游模式（需要用户点击一次页面触发）
    this.autoEnterWalkMode()
  }

  /**
   * 自动进入漫游模式
   * 由于浏览器安全限制，需要用户交互才能锁定鼠标
   */
  autoEnterWalkMode() {
    if (!this.isWalkMode) return
    
    // 添加一次性点击事件来进入漫游
    const enterWalk = () => {
      if (this.isWalkMode && !this.pointerLockControls.isLocked) {
        this.pointerLockControls.lock()
      }
      this.renderer.domElement.removeEventListener('click', enterWalk)
    }
    
    this.renderer.domElement.addEventListener('click', enterWalk)
  }

  /**
   * 创建WebGL渲染器
   */
  createRenderer() {
    this.renderer = new THREE.WebGLRenderer({
      antialias: true,
      alpha: true
    })
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
    //设置像素比,限制最大为2，避免高分屏性能问题
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
    this.renderer.shadowMap.enabled = true//	开启阴影渲染
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap//使用PCF软阴影，边缘更柔和
    this.renderer.outputColorSpace = THREE.SRGBColorSpace//输出sRGB色彩空间，颜色更准确
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping//ACES电影级色调映射，提升画面质感
    this.renderer.toneMappingExposure = 1//曝光度1.2，略微提亮画面
    this.container.appendChild(this.renderer.domElement)
  }

  /**
   * 创建场景
   */
  createScene() {
    this.scene = new THREE.Scene()
    this.scene.background = new THREE.Color(0x1a1a2e)
    this.scene.fog = new THREE.Fog(0x1a1a2e, 50, 200)//线性雾效，距离相机越远越模糊
  }

  /**
   * 创建相机
   */
  createCamera() {
    const aspect = this.container.clientWidth / this.container.clientHeight
    this.camera = new THREE.PerspectiveCamera(60, aspect, 0.1, 1000)//视角  宽高比  近裁面 远裁面
    this.camera.position.copy(this.initialCameraPosition)//复制向量设置相机位置
    this.camera.lookAt(this.initialCameraTarget)//	相机朝向目标点
  }

  /**
   * 创建灯光
   */
  createLights() {
    // 强环境光 - 提供基础照明
    const ambientLight = new THREE.AmbientLight(0xffffff, 1.5)
    this.scene.add(ambientLight)

    // 主方向光
    const directionalLight = new THREE.DirectionalLight(0xffffff, 1.5)
    directionalLight.position.set(10, 30, 10)
    directionalLight.castShadow = true
    directionalLight.shadow.mapSize.width = 2048
    directionalLight.shadow.mapSize.height = 2048
    directionalLight.shadow.camera.near = 0.5
    directionalLight.shadow.camera.far = 100
    directionalLight.shadow.camera.left = -50
    directionalLight.shadow.camera.right = 50
    directionalLight.shadow.camera.top = 50
    directionalLight.shadow.camera.bottom = -50
    this.scene.add(directionalLight)

    // 补光 - 从另一侧照亮
    const fillLight = new THREE.DirectionalLight(0xffffff, 1.0)
    fillLight.position.set(-10, 20, -10)
    this.scene.add(fillLight)

    // 顶部补光
    const topLight = new THREE.DirectionalLight(0xffffff, 0.8)
    topLight.position.set(0, 30, 0)
    this.scene.add(topLight)

    // 半球光 - 模拟天空和地面反射
    const hemisphereLight = new THREE.HemisphereLight(0xffffff, 0x888888, 1.0)
    this.scene.add(hemisphereLight)
  }

  /**
   * 创建地面位置指示器（蓝色光圈）
   */
  createPositionIndicator() {
    // 创建外圈
    const outerRingGeometry = new THREE.RingGeometry(0.4, 0.5, 32)
    const outerRingMaterial = new THREE.MeshBasicMaterial({
      color: 0x00aaff,
      transparent: true,
      opacity: 0.8,
      side: THREE.DoubleSide
    })
    const outerRing = new THREE.Mesh(outerRingGeometry, outerRingMaterial)
    
    // 创建内圈
    const innerRingGeometry = new THREE.RingGeometry(0.15, 0.25, 32)
    const innerRingMaterial = new THREE.MeshBasicMaterial({
      color: 0x00ddff,
      transparent: true,
      opacity: 0.6,
      side: THREE.DoubleSide
    })
    const innerRing = new THREE.Mesh(innerRingGeometry, innerRingMaterial)
    
    // 创建中心点
    const centerGeometry = new THREE.CircleGeometry(0.08, 16)
    const centerMaterial = new THREE.MeshBasicMaterial({
      color: 0xffffff,
      transparent: true,
      opacity: 0.9,
      side: THREE.DoubleSide
    })
    const center = new THREE.Mesh(centerGeometry, centerMaterial)
    
    // 创建组合
    this.positionIndicator = new THREE.Group()
    this.positionIndicator.add(outerRing)
    this.positionIndicator.add(innerRing)
    this.positionIndicator.add(center)
    
    // 旋转使其平放在地面
    this.positionIndicator.rotation.x = -Math.PI / 2
    
    // 初始位置设在相机位置下方
    this.positionIndicator.position.set(
      this.camera.position.x,
      0.01,  // 略高于地面避免z-fighting
      this.camera.position.z
    )
    
    this.scene.add(this.positionIndicator)
    
    // 添加动画效果
    this.indicatorAnimationTime = 0
  }

  /**
   * 更新位置指示器动画
   */
  updatePositionIndicator(delta) {
    if (!this.positionIndicator) return
    
    this.indicatorAnimationTime += delta * 2
    
    // 脉冲效果
    const pulse = Math.sin(this.indicatorAnimationTime) * 0.1 + 1
    this.positionIndicator.children[0].scale.set(pulse, pulse, 1)
    
    // 旋转效果
    this.positionIndicator.children[1].rotation.z += delta * 0.5
  }

  /**
   * 移动位置指示器到指定点，并开始移动相机到该位置
   */
  movePositionIndicator(point) {
    if (!this.positionIndicator) return
    
    // 动画移动到新位置
    const targetY = point.y + 0.01  // 略高于点击位置
    this.positionIndicator.position.set(point.x, targetY, point.z)
    
    // 设置移动目标（保持当前高度）
    const cameraY = this.camera.position.y
    this.moveTarget = new THREE.Vector3(point.x, cameraY, point.z)
    this.isMovingToTarget = true
    
    console.log('%c开始移动到:', 'color: #00aaff', point)
  }

  /**
   * 更新相机移动到目标位置
   */
  updateMoveToTarget(delta) {
    if (!this.isMovingToTarget || !this.moveTarget) return
    
    const cameraPos = this.camera.position
    const target = this.moveTarget
    
    // 计算距离
    const dx = target.x - cameraPos.x
    const dz = target.z - cameraPos.z
    const distance = Math.sqrt(dx * dx + dz * dz)
    
    // 到达目标
    if (distance < 0.1) {
      this.isMovingToTarget = false
      this.moveTarget = null
      console.log('%c到达目标位置', 'color: #4CAF50')
      return
    }
    
    // 计算移动方向和步长
    const moveStep = Math.min(this.moveSpeed * delta, distance)
    const ratio = moveStep / distance
    
    // 计算移动向量
    const moveVector = new THREE.Vector3(dx * ratio, 0, dz * ratio)
    
    // 碰撞检测
    if (this.checkMoveCollision(cameraPos, moveVector)) {
      // 有碰撞，停止移动
      this.isMovingToTarget = false
      this.moveTarget = null
      console.log('%c碰撞，停止移动', 'color: #ff9800')
      return
    }
    
    // 无碰撞，移动相机
    cameraPos.x += moveVector.x
    cameraPos.z += moveVector.z
  }

  /**
   * 点击移动的碰撞检测
   */
  checkMoveCollision(position, moveVector) {
    if (this.collisionObjects.length === 0) return false
    
    const direction = moveVector.clone().normalize()
    
    // 从当前位置发射射线
    this.raycaster.set(position, direction)
    this.raycaster.far = moveVector.length() + 0.5
    
    const intersects = this.raycaster.intersectObjects(this.collisionObjects, true)
    
    // 如果有碰撞且距离太近
    if (intersects.length > 0 && intersects[0].distance < moveVector.length() + 0.3) {
      return true  // 有碰撞
    }
    
    return false  // 无碰撞
  }

  /**
   * 判断一个对象名称是否为有效的展品名称
   * 必须包含'3d'且包含中文字符，排除UUID等无意义名称
   */
  isValidExhibitName(name) {
    if (!name) return false
    const lowerName = name.toLowerCase()
    if (!lowerName.includes('3d')) return false
    // 必须包含中文字符
    if (!/[\u4e00-\u9fa5]/.test(name)) return false
    return true
  }

  /**
   * 为展品创建标记图标
   * 遍历模型找到所有名称包含"3d"且含中文的展品组，在其上方添加可见标记
   */
  createExhibitMarkers() {
    if (!this.hallModel) return
    
    // 找到所有展品组
    this.hallModel.traverse((child) => {
      if (this.isValidExhibitName(child.name)) {
        // 避免重复添加，也排除已有展品组的子节点
        const isChildOfExisting = this.exhibitGroups.some(g => {
          let parent = child.parent
          while (parent) {
            if (parent === g) return true
            parent = parent.parent
          }
          return false
        })
        if (!isChildOfExisting && !this.exhibitGroups.includes(child)) {
          this.exhibitGroups.push(child)
        }
      }
    })
    
    console.log('%c找到展品数量:', 'color: #FF9800; font-weight: bold', this.exhibitGroups.length)
    
    // 为每个展品创建标记
    this.exhibitGroups.forEach((exhibitGroup, index) => {
      const box = new THREE.Box3().setFromObject(exhibitGroup)
      const center = box.getCenter(new THREE.Vector3())
      const size = box.getSize(new THREE.Vector3())
      const wp = new THREE.Vector3()
      exhibitGroup.getWorldPosition(wp)
      console.log(`  ${index + 1}. ${exhibitGroup.name} | 中心: (${center.x.toFixed(2)}, ${center.y.toFixed(2)}, ${center.z.toFixed(2)}) | 尺寸: (${size.x.toFixed(2)}, ${size.y.toFixed(2)}, ${size.z.toFixed(2)}) | 世界坐标: (${wp.x.toFixed(2)}, ${wp.y.toFixed(2)}, ${wp.z.toFixed(2)}) | 父节点: ${exhibitGroup.parent?.name || 'none'}`)
      
      const marker = this.createSingleMarker(exhibitGroup)
      if (marker) {
        this.exhibitMarkers.push(marker)
        this.scene.add(marker)
      }
    })
  }

  /**
   * 为单个展品创建标记
   */
  createSingleMarker(exhibitGroup) {
    // 强制更新世界矩阵，确保所有变换都已应用
    exhibitGroup.updateWorldMatrix(true, true)
    
    // 计算展品的边界盒
    const box = new THREE.Box3().setFromObject(exhibitGroup)
    const center = box.getCenter(new THREE.Vector3())
    const size = box.getSize(new THREE.Vector3())
    
    // 获取展品组的世界坐标
    const worldPos = new THREE.Vector3()
    exhibitGroup.getWorldPosition(worldPos)
    
    // 判断使用哪个位置：
    // 1. 世界坐标在原点附近 → 用边界盒中心
    // 2. 世界坐标超出展厅合理范围 → 用边界盒中心
    // 3. 世界坐标和边界盒中心接近 → 用边界盒中心（更精确）
    // 4. 世界坐标和边界盒中心差距大且世界坐标在合理范围内 → 用世界坐标
    const worldNearOrigin = Math.abs(worldPos.x) < 1 && Math.abs(worldPos.z) < 1
    const worldOutOfRange = Math.abs(worldPos.x) > 25 || Math.abs(worldPos.z) > 115
    const diffX = Math.abs(worldPos.x - center.x)
    const diffZ = Math.abs(worldPos.z - center.z)
    // 只有当差距很大（>10）时才认为边界盒中心不可靠，需要用世界坐标
    const largeGap = (diffX > 10 || diffZ > 10)
    
    let markerX, markerZ, markerY
    
    if (worldNearOrigin || worldOutOfRange) {
      // 世界坐标不可靠，使用边界盒中心
      markerX = center.x
      markerZ = center.z
    } else if (largeGap) {
      // 世界坐标和边界盒差距大，且世界坐标在合理范围内 → 用世界坐标
      // 这种情况是子mesh几何体偏移了（如文房四宝在GLTF_SceneRootNode006下）
      markerX = worldPos.x
      markerZ = worldPos.z
    } else {
      // 差距小，用边界盒中心（更精确）
      markerX = center.x
      markerZ = center.z
    }
    
    markerY = box.max.y + 0.5
    
    console.log(`  标记位置: (${markerX.toFixed(2)}, ${markerY.toFixed(2)}, ${markerZ.toFixed(2)}) | 世界坐标: (${worldPos.x.toFixed(2)}, ${worldPos.y.toFixed(2)}, ${worldPos.z.toFixed(2)}) | 边界盒中心: (${center.x.toFixed(2)}, ${center.y.toFixed(2)}, ${center.z.toFixed(2)})`)
    
    // 创建标记组
    const markerGroup = new THREE.Group()
    markerGroup.userData.exhibitName = exhibitGroup.name
    markerGroup.userData.exhibitGroup = exhibitGroup
    
    // 外圈 - 发光圆环
    const outerRingGeo = new THREE.RingGeometry(0.15, 0.2, 32)
    const outerRingMat = new THREE.MeshBasicMaterial({
      color: 0x00ffaa,
      transparent: true,
      opacity: 0.9,
      side: THREE.DoubleSide
    })
    const outerRing = new THREE.Mesh(outerRingGeo, outerRingMat)
    
    // 内圈
    const innerRingGeo = new THREE.RingGeometry(0.05, 0.1, 32)
    const innerRingMat = new THREE.MeshBasicMaterial({
      color: 0xffffff,
      transparent: true,
      opacity: 0.8,
      side: THREE.DoubleSide
    })
    const innerRing = new THREE.Mesh(innerRingGeo, innerRingMat)
    
    // 中心点
    const centerGeo = new THREE.CircleGeometry(0.03, 16)
    const centerMat = new THREE.MeshBasicMaterial({
      color: 0x00ffaa,
      side: THREE.DoubleSide
    })
    const centerDot = new THREE.Mesh(centerGeo, centerMat)
    
    // 垂直线条指向展品
    const lineGeo = new THREE.BufferGeometry().setFromPoints([
      new THREE.Vector3(0, 0, 0),
      new THREE.Vector3(0, -0.25, 0)
    ])
    const lineMat = new THREE.LineBasicMaterial({
      color: 0x00ffaa,
      transparent: true,
      opacity: 0.6
    })
    const line = new THREE.Line(lineGeo, lineMat)
    
    markerGroup.add(outerRing)
    markerGroup.add(innerRing)
    markerGroup.add(centerDot)
    markerGroup.add(line)
    
    // 让标记水平放置
    markerGroup.rotation.x = -Math.PI / 2
    
    // 设置位置
    markerGroup.position.set(markerX, markerY, markerZ)
    
    // 添加到可点击列表
    this.clickableObjects.push(outerRing, innerRing, centerDot)
    
    return markerGroup
  }

  /**
   * 更新展品标记动画
   */
  updateExhibitMarkers(delta) {
    const time = Date.now() * 0.001
    
    this.exhibitMarkers.forEach((marker, index) => {
      // 上下浮动效果
      const floatOffset = Math.sin(time * 2 + index) * 0.05
      const baseY = marker.userData.baseY || marker.position.y
      if (!marker.userData.baseY) marker.userData.baseY = baseY
      marker.position.y = baseY + floatOffset
      
      // 旋转效果
      if (marker.children[0]) {
        marker.children[0].rotation.z += delta * 0.5
      }
      if (marker.children[1]) {
        marker.children[1].rotation.z -= delta * 0.8
      }
    })
  }

  /**
   * 创建控制器
   */
  createControls() {
    // 轨道控制器 - 自由视角模式
    this.orbitControls = new OrbitControls(this.camera, this.renderer.domElement)
    this.orbitControls.enableDamping = true
    this.orbitControls.dampingFactor = 0.05
    this.orbitControls.minDistance = 1
    this.orbitControls.maxDistance = 100
    this.orbitControls.maxPolarAngle = Math.PI * 0.85
    this.orbitControls.enabled = false

    // 指针锁定控制器 - 第一人称漫游模式
    this.pointerLockControls = new PointerLockControls(this.camera, this.renderer.domElement)
    this.scene.add(this.pointerLockControls.getObject())

    // 手动限制垂直视角范围和灵敏度，并添加平滑过渡
    // PointerLockControls v0.162 不支持 minPolarAngle/maxPolarAngle，需手动拦截
    const _PI_2 = Math.PI / 2
    const minPolar = Math.PI * 0.15  // 最多仰视约27°
    const maxPolar = Math.PI * 0.85  // 最多俯视约27°
    const pointerSpeed = 0.5         // 降低鼠标灵敏度
    const camera = this.camera

    // 平滑插值：用目标欧拉角 + 每帧 lerp 逼近，避免一跳一跳
    this._targetEuler = new THREE.Euler(0, 0, 0, 'YXZ')
    this._currentEuler = new THREE.Euler(0, 0, 0, 'YXZ')
    this._targetEuler.setFromQuaternion(camera.quaternion)
    this._currentEuler.copy(this._targetEuler)
    this._rotationSmoothing = 0.15  // 插值系数，越小越平滑（0.1~0.3）

    // 移除原有的mousemove监听，替换为受限版本
    document.removeEventListener('mousemove', this.pointerLockControls._onMouseMove)
    this.pointerLockControls._onMouseMove = (event) => {
      if (!this.pointerLockControls.isLocked) return
      const movementX = event.movementX || 0
      const movementY = event.movementY || 0
      // 累加到目标角度
      this._targetEuler.y -= movementX * 0.002 * pointerSpeed
      this._targetEuler.x -= movementY * 0.002 * pointerSpeed
      // 限制垂直角度
      this._targetEuler.x = Math.max(_PI_2 - maxPolar, Math.min(_PI_2 - minPolar, this._targetEuler.x))
    }
    document.addEventListener('mousemove', this.pointerLockControls._onMouseMove)

    // 默认使用第一人称模式
    this.controls = this.pointerLockControls
  }

  /**
   * 加载展厅GLB模型
   */
  async loadHallModel() {
    return new Promise((resolve, reject) => {
      const loader = new GLTFLoader()
      
      // 配置Draco解码器（用于压缩模型）
      const dracoLoader = new DRACOLoader()
      dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.6/')
      loader.setDRACOLoader(dracoLoader)

      // 模型路径 - 使用相对于public目录的路径，对中文进行URL编码
      const modelPath = encodeURI('/models/exhibition-hall.glb')

      loader.load(
        modelPath,
        (gltf) => {
          this.onProgress(80, '处理模型材质...')
          
          this.hallModel = gltf.scene
          
          // 调整模型缩放和位置
          this.hallModel.scale.set(1, 1, 1)
          this.hallModel.position.set(0, 0, 0)
          
          // 遍历模型设置属性并记录结构
          this.logModelStructure(this.hallModel)
          
          this.hallModel.traverse((child) => {
            if (child.isMesh) {
              // 启用阴影
              child.castShadow = true
              child.receiveShadow = true
              
              // 添加到碰撞检测列表
              this.collisionObjects.push(child)
              
              // 添加到可点击列表
              this.clickableObjects.push(child)
              
              // 检测地面物体（名称包含floor、地板、ground等）
              const name = (child.name || '').toLowerCase()
              if (name.includes('floor') || name.includes('地板') || name.includes('地') || name.includes('ground') || name.includes('底')) {
                this.floorObjects.push(child)
              }
              
              // 优化材质 - 降级复杂材质以避免Shader Error 1282
              if (child.material) {
                const mats = Array.isArray(child.material) ? child.material : [child.material]
                const newMats = mats.map(mat => {
                  if (mat.isMeshPhysicalMaterial) {
                    // MeshPhysicalMaterial 着色器复杂度高，降级为 MeshStandardMaterial
                    const stdMat = new THREE.MeshStandardMaterial({
                      color: mat.color,
                      map: mat.map,
                      normalMap: mat.normalMap,
                      roughnessMap: mat.roughnessMap,
                      metalnessMap: mat.metalnessMap,
                      aoMap: mat.aoMap,
                      emissiveMap: mat.emissiveMap,
                      emissive: mat.emissive,
                      roughness: mat.roughness,
                      metalness: mat.metalness,
                      transparent: mat.transparent,
                      opacity: mat.opacity,
                      side: THREE.DoubleSide,
                      name: mat.name
                    })
                    mat.dispose()
                    return stdMat
                  }
                  mat.side = THREE.DoubleSide
                  return mat
                })
                child.material = Array.isArray(child.material) ? newMats : newMats[0]
              }
            }
          })
          
          this.scene.add(this.hallModel)
          
          // 创建位置指示器
          this.createPositionIndicator()
          
          // 为展品创建标记图标
          this.createExhibitMarkers()
          
          // 设置视频屏幕
          this.setupVideoScreen()
          
          this.onProgress(90, '计算模型边界...')
          // 计算模型边界，调整相机位置
          this.adjustCameraToModel()
          
          resolve()
        },
        (xhr) => {
          const progress = 30 + (xhr.loaded / xhr.total) * 50
          this.onProgress(Math.min(progress, 80), '加载模型数据...')
        },
        (error) => {
          console.error('模型加载失败:', error)
          this.onProgress(100, '模型加载失败，请确保模型文件存在')
          // 不创建默认场景，直接提示错误
          reject(new Error('GLB模型加载失败，请将3d展厅稿11.glb文件放置到public/models目录'))
        }
      )
    })
  }


  /**
   * 遍历并打印模型结构
   * 用于了解模型中有哪些物体可以绑定点击事件
   */
  logModelStructure(model, depth = 0) {
    const indent = '  '.repeat(depth)
    const info = {
      name: model.name || '(unnamed)',
      type: model.type,
      uuid: model.uuid,
      children: []
    }
    
    if (depth === 0) {
      console.log('%c========== 模型结构 ==========', 'color: #4CAF50; font-weight: bold')
    }
    
    // 打印当前节点信息
    let logStyle = 'color: #888'
    if (model.isMesh) {
      logStyle = 'color: #2196F3; font-weight: bold'
    } else if (model.isGroup) {
      logStyle = 'color: #FF9800'
    }
    
    console.log(
      `%c${indent}├─ ${model.name || '(unnamed)'} [${model.type}]`,
      logStyle,
      model.isMesh ? `| 顶点数: ${model.geometry?.attributes?.position?.count || 0}` : ''
    )
    
    // 递归遍历子节点
    if (model.children && model.children.length > 0) {
      model.children.forEach(child => {
        const childInfo = this.logModelStructure(child, depth + 1)
        info.children.push(childInfo)
      })
    }
    
    if (depth === 0) {
      console.log('%c===================================', 'color: #4CAF50; font-weight: bold')
      this.modelStructure = info
      
      // 输出可点击物体汇总
      console.log('%c可点击物体列表:', 'color: #E91E63; font-weight: bold')
      this.clickableObjects.forEach((obj, index) => {
        console.log(`  ${index + 1}. ${obj.name || '(unnamed)'} - UUID: ${obj.uuid}`)
      })
    }
    
    return info
  }

  /**
   * 获取模型结构信息
   * @returns {Object} 模型结构树
   */
  getModelStructure() {
    return this.modelStructure
  }

  /**
   * 获取所有可点击物体的名称列表
   * @returns {Array} 物体名称列表
   */
  getClickableObjectNames() {
    return this.clickableObjects.map(obj => ({
      name: obj.name || '(unnamed)',
      uuid: obj.uuid
    }))
  }

  /**
   * 根据模型调整相机位置
   * 将相机放置在展厅内部合适位置
   */
  adjustCameraToModel() {
    if (!this.hallModel) return

    const box = new THREE.Box3().setFromObject(this.hallModel)
    const center = box.getCenter(new THREE.Vector3())
    const size = box.getSize(new THREE.Vector3())

    console.log('展厅模型尺寸:', size)
    console.log('展厅模型中心:', center)

    // 将相机放置在展厅内部
    // 人眼高度约1.6-1.8米
    const eyeHeight = 1.0
    
    // 设置初始位置为模型中心，人眼高度
    this.initialCameraPosition.set(center.x, center.y + eyeHeight, center.z)
    this.initialCameraTarget.set(center.x, center.y + eyeHeight, center.z - 5)

    this.camera.position.copy(this.initialCameraPosition)
    this.camera.lookAt(this.initialCameraTarget)

    // 更新轨道控制器目标点
    this.orbitControls.target.copy(this.initialCameraTarget)
    
    // 创建天花板
    this.createCeiling()
  }

  /**
   * 创建天花板
   * 根据模型边界自动生成匹配的天花板
   * 第一人称模式显示，自由视角模式隐藏
   */
  createCeiling() {
    if (!this.hallModel) return
    
    const box = new THREE.Box3().setFromObject(this.hallModel)
    const size = box.getSize(new THREE.Vector3())
    const center = box.getCenter(new THREE.Vector3())
    
    // 创建平面几何体作为天花板
    const geometry = new THREE.PlaneGeometry(size.x * 1.2, size.z * 1.2)
    const material = new THREE.MeshBasicMaterial({
      color: 0xcccccc,  // 白灰色
      side: THREE.DoubleSide
    })
    
    this.ceiling = new THREE.Mesh(geometry, material)
    // 旋转使其水平（面朝下）
    this.ceiling.rotation.x = Math.PI / 2
    // 放置在模型顶部
    this.ceiling.position.set(center.x, box.max.y + 0.1, center.z)
    
    // 默认根据当前模式设置可见性（第一人称显示，自由视角隐藏）
    this.ceiling.visible = this.isWalkMode
    
    this.scene.add(this.ceiling)
    
    // console.log('天花板已创建，位置:', this.ceiling.position)
  }

  /**
   * 初始化小地图
   */
  initMinimap() {
    if (!this.minimapCanvas) return

    this.minimapCanvas.width = 150
    this.minimapCanvas.height = 150
    this.minimapContext = this.minimapCanvas.getContext('2d')
  }

  /**
   * 更新小地图
   */
  updateMinimap() {
    if (!this.minimapContext) return

    const ctx = this.minimapContext
    const width = this.minimapCanvas.width
    const height = this.minimapCanvas.height

    // 清空画布
    ctx.fillStyle = 'rgba(0, 0, 0, 0.8)'
    ctx.fillRect(0, 0, width, height)

    // 绘制简单的展厅轮廓
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)'
    ctx.lineWidth = 1
    ctx.strokeRect(20, 20, width - 40, height - 40)

    // 绘制玩家位置
    const playerX = width / 2 + (this.camera.position.x / 50) * (width / 2 - 20)
    const playerZ = height / 2 + (this.camera.position.z / 50) * (height / 2 - 20)

    ctx.fillStyle = '#4CAF50'
    ctx.beginPath()
    ctx.arc(playerX, playerZ, 5, 0, Math.PI * 2)
    ctx.fill()

    // 绘制朝向
    const direction = new THREE.Vector3()
    this.camera.getWorldDirection(direction)
    
    ctx.strokeStyle = '#4CAF50'
    ctx.lineWidth = 2
    ctx.beginPath()
    ctx.moveTo(playerX, playerZ)
    ctx.lineTo(playerX + direction.x * 15, playerZ + direction.z * 15)
    ctx.stroke()
  }

  /**
   * 绑定事件
   */
  bindEvents() {
    // 窗口大小变化
    window.addEventListener('resize', this.onWindowResize.bind(this))

    // 键盘事件
    document.addEventListener('keydown', this.onKeyDown.bind(this))
    document.addEventListener('keyup', this.onKeyUp.bind(this))

    // 点击事件处理
    this.renderer.domElement.addEventListener('click', (event) => {
      // 第一人称模式下
      if (this.isWalkMode) {
        if (this.pointerLockControls.isLocked) {
          // 已锁定，使用屏幕中心点检测展品
          this.handleFirstPersonClick()
        } else {
          // 未锁定，点击画布重新进入漫游
          this.pointerLockControls.lock()
        }
        return
      }
      
      // 自由视角模式，使用鼠标位置检测
      this.handleObjectClick(event)
    })

    // 指针锁定状态变化
    this.pointerLockControls.addEventListener('lock', () => {
      console.log('进入漫游模式')
      // 隐藏光标
      this.renderer.domElement.style.cursor = 'none'
    })

    this.pointerLockControls.addEventListener('unlock', () => {
      console.log('退出漫游模式 - 点击画布可重新进入')
      this.renderer.domElement.style.cursor = 'default'
    })
    
    // 鼠标移动事件 - 检测展品悬停（仅自由视角模式）
    this.renderer.domElement.addEventListener('mousemove', (event) => {
      if (this.isWalkMode && this.pointerLockControls.isLocked) return
      this.handleMouseMove(event)
    })
  }

  /**
   * 处理鼠标移动，检测展品悬停
   */
  handleMouseMove(event) {
    const rect = this.renderer.domElement.getBoundingClientRect()
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

    this.clickRaycaster.setFromCamera(this.mouse, this.camera)
    const intersects = this.clickRaycaster.intersectObjects(this.clickableObjects, true)

    let foundExhibit = null
    let foundVideo = null
    
    for (const intersect of intersects) {
      const obj = intersect.object
      if (this.shouldPenetrateObject(obj)) continue
      
      // 检查是否是视频屏幕
      if (obj.userData && obj.userData.isVideoScreen) {
        foundVideo = obj.userData.videoName
        break
      }
      
      // 向上查找展品组
      let target = obj
      while (target) {
        if (this.isValidExhibitName(target.name)) {
          foundExhibit = target
          break
        }
        target = target.parent
      }
      if (foundExhibit) break
    }

    // 更新视频悬停状态
    if (foundVideo !== this.hoveredVideo) {
      this.hoveredVideo = foundVideo
      if (this.onVideoHover) {
        this.onVideoHover(foundVideo)
      }
    }

    // 更新悬停状态
    if (foundExhibit !== this.hoveredExhibit) {
      // 取消之前的高亮
      if (this.hoveredExhibit) {
        this.unhighlightExhibit(this.hoveredExhibit)
      }
      
      // 高亮新展品
      if (foundExhibit) {
        this.highlightExhibit(foundExhibit)
        this.renderer.domElement.style.cursor = 'pointer'
      } else if (!foundVideo) {
        this.renderer.domElement.style.cursor = 'default'
      }
      
      if (foundVideo) {
        this.renderer.domElement.style.cursor = 'pointer'
      }
      
      this.hoveredExhibit = foundExhibit
      // 触发展品悬停回调（自由视角模式也显示提示）
      if (this.onExhibitHover) {
        this.onExhibitHover(foundExhibit ? foundExhibit.name : null)
      }
    }
  }

  /**
   * 高亮展品
   */
  highlightExhibit(exhibitGroup) {
    exhibitGroup.traverse((child) => {
      if (child.isMesh && child.material) {
        // 保存原始材质
        if (!this.originalMaterials.has(child.uuid)) {
          this.originalMaterials.set(child.uuid, child.material.clone())
        }
        
        // 添加发光效果
        if (Array.isArray(child.material)) {
          child.material = child.material.map(mat => {
            const newMat = mat.clone()
            newMat.emissive = new THREE.Color(0x00aaff)
            newMat.emissiveIntensity = 0.3
            return newMat
          })
        } else {
          const newMat = child.material.clone()
          newMat.emissive = new THREE.Color(0x00aaff)
          newMat.emissiveIntensity = 0.3
          child.material = newMat
        }
      }
    })
  }

  /**
   * 取消展品高亮
   */
  unhighlightExhibit(exhibitGroup) {
    exhibitGroup.traverse((child) => {
      if (child.isMesh && this.originalMaterials.has(child.uuid)) {
        child.material = this.originalMaterials.get(child.uuid)
        this.originalMaterials.delete(child.uuid)
      }
    })
  }

  /**
   * 检查物体是否应该被射线穿透（透明物体、玻璃罩等）
   */
  shouldPenetrateObject(object) {
    const name = (object.name || '').toLowerCase()
    // 检查名称是否包含玻璃、罩、透明等关键词
    const penetrateKeywords = ['玻璃', 'glass', '罩', 'cover', '透明', 'transparent', 'shield']
    for (const keyword of penetrateKeywords) {
      if (name.includes(keyword)) return true
    }
    // 检查材质是否透明
    if (object.material) {
      const mat = object.material
      if (mat.transparent && mat.opacity < 0.9) return true
      if (mat.alphaTest > 0) return true
    }
    return false
  }

  /**
   * 检查物体是否为地面
   */
  isFloorObject(object) {
    const name = (object.name || '').toLowerCase()
    return name.includes('floor') || name.includes('地板') || name.includes('地') || name.includes('ground') || name.includes('底') || this.floorObjects.includes(object)
  }

  /**
   * 从射线检测结果中找到有效的展品
   */
  findExhibitFromIntersects(intersects) {
    // 记录第一个地面点击位置
    let floorClickPoint = null
    
    for (const intersect of intersects) {
      const clickedObject = intersect.object
      
      // 检查是否点击了视频屏幕
      if (clickedObject.userData && clickedObject.userData.isVideoScreen) {
        return {
          name: clickedObject.userData.videoName,
          uuid: clickedObject.uuid,
          type: 'VideoScreen',
          position: intersect.point,
          object: clickedObject,
          parentName: null,
          isExhibit: false,
          isVideoScreen: true,
          videoSrc: clickedObject.userData.videoSrc,
          isFloor: false,
          floorClickPoint: null
        }
      }
      
      // 检查是否点击了展品标记
      let markerParent = clickedObject.parent
      while (markerParent) {
        if (markerParent.userData && markerParent.userData.exhibitName) {
          // 点击了展品标记，返回对应展品信息
          return {
            name: markerParent.userData.exhibitName,
            uuid: clickedObject.uuid,
            type: 'ExhibitMarker',
            position: intersect.point,
            object: markerParent.userData.exhibitGroup,
            parentName: null,
            isExhibit: true,
            isFloor: false,
            floorClickPoint: null
          }
        }
        markerParent = markerParent.parent
      }
      
      // 检查是否点击了地面
      if (this.isFloorObject(clickedObject) && !floorClickPoint) {
        floorClickPoint = intersect.point.clone()
      }
      
      // 跳过应该穿透的物体（玻璃罩等）
      if (this.shouldPenetrateObject(clickedObject)) {
        console.log('穿透透明物体:', clickedObject.name)
        continue
      }
      
      // 向上查找有效展品名称的父物体（展品组）
      let targetObject = clickedObject
      let exhibitGroup = null
      
      while (targetObject) {
        if (this.isValidExhibitName(targetObject.name)) {
          exhibitGroup = targetObject
          break
        }
        targetObject = targetObject.parent
      }
      
      // 找到展品，返回信息
      if (exhibitGroup) {
        return {
          name: exhibitGroup.name,
          uuid: clickedObject.uuid,
          type: clickedObject.type,
          position: intersect.point,
          object: clickedObject,
          parentName: clickedObject.parent?.name || null,
          isExhibit: true,
          isFloor: false,
          floorClickPoint: null
        }
      }
    }
    
    // 没找到展品，返回第一个非穿透物体
    for (const intersect of intersects) {
      if (!this.shouldPenetrateObject(intersect.object)) {
        const obj = intersect.object
        const isFloor = this.isFloorObject(obj)
        let targetObject = obj
        while (targetObject && !targetObject.name && targetObject.parent) {
          targetObject = targetObject.parent
        }
        return {
          name: targetObject.name || obj.name || '(unnamed)',
          uuid: obj.uuid,
          type: obj.type,
          position: intersect.point,
          object: obj,
          parentName: obj.parent?.name || null,
          isExhibit: false,
          isFloor: isFloor,
          floorClickPoint: isFloor ? intersect.point.clone() : floorClickPoint
        }
      }
    }
    return null
  }

  /**
   * 处理物体点击事件
   * @param {MouseEvent} event 鼠标事件
   */
  handleObjectClick(event) {
    // 计算鼠标在归一化设备坐标中的位置
    const rect = this.renderer.domElement.getBoundingClientRect()
    this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1
    this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1

    // 通过相机和鼠标位置更新射线
    this.clickRaycaster.setFromCamera(this.mouse, this.camera)

    // 检测射线与可点击物体的相交
    const intersects = this.clickRaycaster.intersectObjects(this.clickableObjects, true)

    if (intersects.length > 0) {
      const objectInfo = this.findExhibitFromIntersects(intersects)
      
      if (objectInfo) {
        // 如果点击了地面，移动位置指示器
        if (objectInfo.isFloor || objectInfo.floorClickPoint) {
          const floorPoint = objectInfo.floorClickPoint || objectInfo.position
          this.movePositionIndicator(floorPoint)
        }
        
        console.log('%c点击了物体:', 'color: #4CAF50; font-weight: bold', objectInfo.name)
        console.log('  - 是否为展品:', objectInfo.isExhibit)
        console.log('  - 是否为地面:', objectInfo.isFloor)
        this.onObjectClick(objectInfo)
      }
    }
  }

  /**
   * 第一人称模式下的点击检测
   * 使用屏幕中心点进行射线检测
   */
  handleFirstPersonClick() {
    // 第一人称模式下，鼠标锁定在屏幕中心，使用(0,0)作为射线起点
    this.clickRaycaster.setFromCamera(new THREE.Vector2(0, 0), this.camera)

    // 检测射线与可点击物体的相交
    const intersects = this.clickRaycaster.intersectObjects(this.clickableObjects, true)

    if (intersects.length > 0) {
      // 使用相同的穿透逻辑找到有效展品
      const objectInfo = this.findExhibitFromIntersects(intersects)
      
      if (objectInfo) {
        // 如果点击了地面，移动位置指示器
        if (objectInfo.isFloor || objectInfo.floorClickPoint) {
          const floorPoint = objectInfo.floorClickPoint || objectInfo.position
          this.movePositionIndicator(floorPoint)
        }
        
        console.log('%c[第一人称]点击了物体:', 'color: #2196F3; font-weight: bold', objectInfo.name)
        console.log('  - 是否为展品:', objectInfo.isExhibit)
        this.onObjectClick(objectInfo)
      }
    }
  }

  /**
   * 根据物体名称设置点击回调
   * @param {Object} clickHandlers 物体名称到回调函数的映射
   * 例如: { '展品1': () => router.push('/detail/1'), '展品2': () => router.push('/detail/2') }
   */
  setClickHandlers(clickHandlers) {
    this.clickHandlers = clickHandlers
    
    // 更新onObjectClick回调
    this.onObjectClick = (objectInfo) => {
      const handler = this.clickHandlers[objectInfo.name]
      if (handler) {
        handler(objectInfo)
      } else {
        // 尝试匹配部分名称
        for (const [name, callback] of Object.entries(this.clickHandlers)) {
          if (objectInfo.name.includes(name) || name.includes(objectInfo.name)) {
            callback(objectInfo)
            return
          }
        }
        console.log('未找到该物体的点击处理器:', objectInfo.name)
      }
    }
  }

  /**
   * 高亮显示指定物体
   * @param {string} objectName 物体名称
   * @param {number} color 高亮颜色 (默认黄色)
   */
  highlightObject(objectName, color = 0xffff00) {
    const object = this.clickableObjects.find(obj => obj.name === objectName)
    if (object && object.material) {
      // 保存原始材质
      if (!object.userData.originalMaterial) {
        object.userData.originalMaterial = object.material.clone()
      }
      // 设置高亮
      object.material.emissive = new THREE.Color(color)
      object.material.emissiveIntensity = 0.3
    }
  }

  /**
   * 取消物体高亮
   * @param {string} objectName 物体名称
   */
  unhighlightObject(objectName) {
    const object = this.clickableObjects.find(obj => obj.name === objectName)
    if (object && object.userData.originalMaterial) {
      object.material.copy(object.userData.originalMaterial)
    }
  }

  /**
   * 键盘按下事件
   */
  onKeyDown(event) {
    switch (event.code) {
      case 'KeyW':
      case 'ArrowUp':
        this.moveState.forward = true
        break
      case 'KeyS':
      case 'ArrowDown':
        this.moveState.backward = true
        break
      case 'KeyA':
      case 'ArrowLeft':
        this.moveState.left = true
        break
      case 'KeyD':
      case 'ArrowRight':
        this.moveState.right = true
        break
    }
  }

  /**
   * 键盘松开事件
   */
  onKeyUp(event) {
    switch (event.code) {
      case 'KeyW':
      case 'ArrowUp':
        this.moveState.forward = false
        break
      case 'KeyS':
      case 'ArrowDown':
        this.moveState.backward = false
        break
      case 'KeyA':
      case 'ArrowLeft':
        this.moveState.left = false
        break
      case 'KeyD':
      case 'ArrowRight':
        this.moveState.right = false
        break
    }
  }

  /**
   * 窗口大小变化
   */
  onWindowResize() {
    this.camera.aspect = this.container.clientWidth / this.container.clientHeight
    this.camera.updateProjectionMatrix()
    this.renderer.setSize(this.container.clientWidth, this.container.clientHeight)
  }

  /**
   * 更新移动
   */
  updateMovement(delta) {
    if (!this.isWalkMode || !this.pointerLockControls.isLocked) return

    const speed = this.moveSpeed * delta
    const direction = new THREE.Vector3()

    // 获取相机朝向
    this.camera.getWorldDirection(direction)
    direction.y = 0
    direction.normalize()

    // 计算右方向
    const right = new THREE.Vector3()
    right.crossVectors(direction, new THREE.Vector3(0, 1, 0))

    // 计算移动向量
    const moveVector = new THREE.Vector3()

    if (this.moveState.forward) {
      moveVector.add(direction.clone().multiplyScalar(speed))
    }
    if (this.moveState.backward) {
      moveVector.add(direction.clone().multiplyScalar(-speed))
    }
    if (this.moveState.left) {
      moveVector.add(right.clone().multiplyScalar(-speed))
    }
    if (this.moveState.right) {
      moveVector.add(right.clone().multiplyScalar(speed))
    }

    // 碰撞检测
    if (moveVector.length() > 0) {
      const canMove = this.checkCollision(moveVector)
      if (canMove) {
        this.pointerLockControls.getObject().position.add(moveVector)
      }
    }
  }

  /**
   * 碰撞检测
   */
  checkCollision(moveVector) {
    if (this.collisionObjects.length === 0) return true

    const position = this.pointerLockControls.getObject().position.clone()
    const direction = moveVector.clone().normalize()

    // 从角色位置发射射线
    this.raycaster.set(position, direction)
    this.raycaster.far = moveVector.length() + 0.5

    const intersects = this.raycaster.intersectObjects(this.collisionObjects, true)

    // 如果没有碰撞或碰撞距离足够远，允许移动
    if (intersects.length === 0 || intersects[0].distance > moveVector.length() + 0.3) {
      return true
    }

    return false
  }

  /**
   * 设置漫游模式
   */
  setWalkMode(isWalk) {
    this.isWalkMode = isWalk

    // 切换天花板可见性：第一人称显示，自由视角隐藏
    if (this.ceiling) {
      this.ceiling.visible = isWalk
    }

    if (isWalk) {
      this.orbitControls.enabled = false
      // 自动进入漫游锁定
      this.pointerLockControls.lock()
    } else {
      this.pointerLockControls.unlock()
      this.orbitControls.enabled = true
    }
  }

  /**
   * 设置移动速度
   */
  setMoveSpeed(speed) {
    this.moveSpeed = speed
  }

  /**
   * 重置相机
   */
  resetCamera() {
    this.camera.position.copy(this.initialCameraPosition)
    this.camera.lookAt(this.initialCameraTarget)
    
    // 同步平滑旋转的目标和当前角度
    if (this._targetEuler && this._currentEuler) {
      this._targetEuler.setFromQuaternion(this.camera.quaternion)
      this._currentEuler.copy(this._targetEuler)
    }
    
    if (!this.isWalkMode) {
      this.orbitControls.target.copy(this.initialCameraTarget)
      this.orbitControls.update()
    }
  }

  /**
   * 第一人称模式下检测准星悬停的展品
   */
  updateCrosshairHover() {
    if (!this.isWalkMode || !this.pointerLockControls.isLocked) {
      // 非第一人称模式，清除悬停状态
      if (this.hoveredExhibit) {
        this.hoveredExhibit = null
        if (this.onExhibitHover) this.onExhibitHover(null)
      }
      if (this.hoveredVideo) {
        this.hoveredVideo = null
        if (this.onVideoHover) this.onVideoHover(null)
      }
      return
    }
    
    // 使用屏幕中心点检测
    this.clickRaycaster.setFromCamera(new THREE.Vector2(0, 0), this.camera)
    const intersects = this.clickRaycaster.intersectObjects(this.clickableObjects, true)
    
    let foundExhibit = null
    let exhibitName = null
    let foundVideo = null
    
    // 遍历所有相交物体，寻找展品或视频
    for (const intersect of intersects) {
      const obj = intersect.object
      
      // 跳过透明物体
      if (this.shouldPenetrateObject(obj)) continue
      
      // 跳过地面
      if (this.isFloorObject(obj)) continue
      
      // 检查是否是视频屏幕
      if (obj.userData && obj.userData.isVideoScreen) {
        foundVideo = obj.userData.videoName
        break
      }
      
      // 向上查找视频屏幕（可能命中的是子物体）
      let videoParent = obj
      while (videoParent) {
        if (videoParent.userData && videoParent.userData.isVideoScreen) {
          foundVideo = videoParent.userData.videoName
          break
        }
        videoParent = videoParent.parent
      }
      if (foundVideo) break
      
      // 检查是否是展品标记
      let markerParent = obj.parent
      while (markerParent) {
        if (markerParent.userData && markerParent.userData.exhibitName) {
          foundExhibit = markerParent.userData.exhibitGroup
          exhibitName = markerParent.userData.exhibitName
          break
        }
        markerParent = markerParent.parent
      }
      if (foundExhibit) break
      
      // 向上查找有效展品名称的展品组
      let target = obj
      while (target) {
        if (this.isValidExhibitName(target.name)) {
          foundExhibit = target
          exhibitName = target.name
          break
        }
        target = target.parent
      }
      
      // 找到展品就停止
      if (foundExhibit) break
      
      // 如果第一个有效物体不是展品，停止搜索
      break
    }
    
    // 视频悬停状态变化时触发回调
    if (foundVideo !== this.hoveredVideo) {
      this.hoveredVideo = foundVideo
      if (this.onVideoHover) {
        this.onVideoHover(foundVideo)
      }
    }
    
    // 展品悬停状态变化时触发回调
    if (foundExhibit !== this.hoveredExhibit) {
      this.hoveredExhibit = foundExhibit
      if (this.onExhibitHover) {
        this.onExhibitHover(exhibitName)
      }
    }
  }

  /**
   * 平滑旋转插值 —— 每帧将当前角度向目标角度靠近
   */
  updateSmoothRotation() {
    if (!this._targetEuler || !this._currentEuler) return
    if (!this.isWalkMode || !this.pointerLockControls.isLocked) return

    const t = this._rotationSmoothing
    // lerp 当前角度向目标角度过渡
    this._currentEuler.x += (this._targetEuler.x - this._currentEuler.x) * t
    this._currentEuler.y += (this._targetEuler.y - this._currentEuler.y) * t

    this.camera.quaternion.setFromEuler(this._currentEuler)
    this.pointerLockControls.dispatchEvent({ type: 'change' })
  }

  /**
   * 动画循环
   */
  animate() {
    this.animationId = requestAnimationFrame(this.animate.bind(this))

    const delta = this.clock.getDelta()

    // 平滑旋转插值
    this.updateSmoothRotation()

    // 更新移动
    this.updateMovement(delta)

    // 更新控制器
    if (!this.isWalkMode && this.orbitControls.enabled) {
      this.orbitControls.update()
    }

    // 更新位置指示器动画
    this.updatePositionIndicator(delta)

    // 更新点击移动
    this.updateMoveToTarget(delta)

    // 更新展品标记动画
    this.updateExhibitMarkers(delta)
    
    // 第一人称模式下检测准星悬停
    this.updateCrosshairHover()

    // 更新小地图
    this.updateMinimap()

    // 渲染场景
    this.renderer.render(this.scene, this.camera)
  }

  /**
   * 设置所有视频屏幕
   */
  setupVideoScreen() {
    if (!this.hallModel) return
    
    // 视频屏幕配置：模型名称关键字 -> 视频路径
    const videoConfigs = [
      { screenName: '视频屏幕皮影', videoSrc: '/videos/皮影戏.mp4' },
      { screenName: '视频屏幕瓷器', videoSrc: '/videos/瓷器.mp4' }
    ]
    
    videoConfigs.forEach((config) => {
      this.createVideoOnScreen(config.screenName, config.videoSrc)
    })
  }

  /**
   * 在指定模型上创建视频播放
   */
  createVideoOnScreen(screenName, videoSrc) {
    let screenMesh = null
    
    // 查找视频屏幕模型
    this.hallModel.traverse((child) => {
      if (child.isMesh && child.name && child.name.includes(screenName)) {
        console.log('%c找到视频屏幕:', 'color: #9C27B0; font-weight: bold', child.name)
        screenMesh = child
      }
    })
    
    if (!screenMesh) {
      console.log(`%c未找到名为"${screenName}"的模型`, 'color: #FF9800')
      return
    }
    
    // 标记原始屏幕mesh，使其也能被悬停检测识别
    screenMesh.userData.isVideoScreen = true
    screenMesh.userData.videoName = screenName
    screenMesh.userData.videoSrc = videoSrc
    
    // 创建视频元素
    const videoElement = document.createElement('video')
    videoElement.src = videoSrc
    videoElement.crossOrigin = 'anonymous'
    videoElement.loop = true
    videoElement.muted = true
    videoElement.playsInline = true
    videoElement.autoplay = true
    
    // 创建视频纹理
    const videoTexture = new THREE.VideoTexture(videoElement)
    videoTexture.minFilter = THREE.LinearFilter
    videoTexture.magFilter = THREE.LinearFilter
    videoTexture.format = THREE.RGBAFormat
    videoTexture.colorSpace = THREE.SRGBColorSpace
    videoTexture.flipY = true
    // 水平翻转纹理修正镜像问题
    videoTexture.wrapS = THREE.RepeatWrapping
    videoTexture.repeat.x = -1
    
    // 获取屏幕的边界盒来创建匹配的平面
    const box = new THREE.Box3().setFromObject(screenMesh)
    const size = new THREE.Vector3()
    const center = new THREE.Vector3()
    box.getSize(size)
    box.getCenter(center)
    
    console.log(`${screenName} 屏幕尺寸:`, size, '中心:', center)
    
    // 隐藏原始屏幕模型
    screenMesh.visible = false
    
    // 判断屏幕朝向（根据哪个维度最小）
    let planeWidth, planeHeight, rotation
    if (size.z < size.x && size.z < size.y) {
      planeWidth = size.x
      planeHeight = size.y
      rotation = { x: 0, y: 0, z: 0 }
    } else if (size.x < size.y && size.x < size.z) {
      planeWidth = size.z
      planeHeight = size.y
      rotation = { x: 0, y: Math.PI / 2, z: 0 }
    } else {
      planeWidth = size.x
      planeHeight = size.z
      rotation = { x: -Math.PI / 2, y: 0, z: 0 }
    }
    
    // 创建平面几何体
    const planeGeometry = new THREE.PlaneGeometry(planeWidth * 0.98, planeHeight * 0.98)
    
    // 创建视频材质
    const videoMaterial = new THREE.MeshBasicMaterial({
      map: videoTexture,
      side: THREE.DoubleSide
    })
    
    // 创建视频平面
    const videoPlane = new THREE.Mesh(planeGeometry, videoMaterial)
    videoPlane.position.copy(center)
    videoPlane.rotation.set(rotation.x, rotation.y, rotation.z)
    
    // 添加到场景
    this.scene.add(videoPlane)
    
    // 设置userData用于点击检测
    videoPlane.userData.isVideoScreen = true
    videoPlane.userData.videoName = screenName
    videoPlane.userData.videoSrc = videoSrc
    
    // 添加到可点击列表
    this.clickableObjects.push(videoPlane)
    
    // 保存引用以便清理
    this.videoScreens.push({
      name: screenName,
      element: videoElement,
      texture: videoTexture,
      screen: screenMesh,
      plane: videoPlane
    })
    
    // 开始播放视频
    videoElement.play().then(() => {
      console.log(`%c${screenName} 视频开始播放`, 'color: #4CAF50; font-weight: bold')
    }).catch((error) => {
      console.error(`${screenName} 视频播放失败:`, error)
    })
  }

  /**
   * 播放/暂停所有视频
   */
  toggleVideo() {
    this.videoScreens.forEach((vs) => {
      if (vs.element.paused) {
        vs.element.play()
      } else {
        vs.element.pause()
      }
    })
  }

  /**
   * 设置所有视频静音状态
   */
  setVideoMuted(muted) {
    this.videoScreens.forEach((vs) => {
      vs.element.muted = muted
    })
  }

  /**
   * 销毁场景
   */
  dispose() {
    // 停止动画
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
    }

    // 移除事件监听
    window.removeEventListener('resize', this.onWindowResize.bind(this))
    document.removeEventListener('keydown', this.onKeyDown.bind(this))
    document.removeEventListener('keyup', this.onKeyUp.bind(this))

    // 销毁控制器
    if (this.orbitControls) {
      this.orbitControls.dispose()
    }
    if (this.pointerLockControls) {
      this.pointerLockControls.unlock()
    }
    
    // 清理所有视频
    this.videoScreens.forEach((vs) => {
      vs.element.pause()
      vs.element.src = ''
      vs.texture.dispose()
      this.scene.remove(vs.plane)
      vs.plane.geometry.dispose()
      vs.plane.material.dispose()
    })
    this.videoScreens = []

    // 清理场景
    this.scene.traverse((object) => {
      if (object.geometry) {
        object.geometry.dispose()
      }
      if (object.material) {
        if (Array.isArray(object.material)) {
          object.material.forEach((material) => material.dispose())
        } else {
          object.material.dispose()
        }
      }
    })

    // 销毁渲染器
    this.renderer.dispose()
    this.container.removeChild(this.renderer.domElement)
  }
}
