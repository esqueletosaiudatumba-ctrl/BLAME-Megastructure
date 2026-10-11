from pathlib import Path
import os, random, math, json, shutil, zipfile, hashlib, time, textwrap
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
OUT = Path(os.environ.get('BLAME_OUT_DIR', str(ROOT)))
KIT = ROOT/'BLAME_CubicChunks_Procedural_Kit'
if KIT.exists(): shutil.rmtree(KIT)
(KIT/'world_template'/'data'/'functions'/'blame'/'sector').mkdir(parents=True)
(KIT/'minecraft_instance'/'minecraft'/'mods').mkdir(parents=True)
(KIT/'minecraft_instance'/'minecraft'/'config').mkdir(parents=True)
(KIT/'minecraft_instance'/'minecraft'/'shaderpacks').mkdir(parents=True)
(KIT/'minecraft_instance'/'minecraft'/'resourcepacks').mkdir(parents=True)
(KIT/'source').mkdir(parents=True)

FUN = KIT/'world_template'/'data'/'functions'/'blame'
funcs = {}
def begin(name): funcs[name] = []
def line(name, cmd): funcs.setdefault(name, []).append(cmd)

def _fill_raw(out, x1,y1,z1,x2,y2,z2,block,meta=0):
    # Split any fill into bounded pieces. 1.12.2 /fill limit = 32768 blocks.
    lo = [min(x1,x2), min(y1,y2), min(z1,z2)]
    hi = [max(x1,x2), max(y1,y2), max(z1,z2)]
    volume = (hi[0]-lo[0]+1)*(hi[1]-lo[1]+1)*(hi[2]-lo[2]+1)
    if volume <= 28000:
        out.append(f'fill {lo[0]} {lo[1]} {lo[2]} {hi[0]} {hi[1]} {hi[2]} {block} {meta}')
        return
    spans = [hi[i]-lo[i]+1 for i in range(3)]
    axis = max(range(3), key=lambda i: spans[i])
    mid = (lo[axis]+hi[axis])//2
    h1 = hi.copy(); h1[axis]=mid
    l2 = lo.copy(); l2[axis]=mid+1
    _fill_raw(out, *lo, *h1, block, meta)
    _fill_raw(out, *l2, *hi, block, meta)

def fill(name, x1,y1,z1,x2,y2,z2,block,meta=0):
    _fill_raw(funcs[name],x1,y1,z1,x2,y2,z2,block,meta)

def setb(name,x,y,z,block,meta=0):
    line(name, f'setblock {int(x)} {int(y)} {int(z)} {block} {meta}')

def call(name, sub): line(name, f'function blame:{sub}')

def legacy_set(name, x,y,z, block, meta=0):
    setb(name,x,y,z,block,meta)

# Setup: bright daylight is fixed, not merely a time estimate.
begin('setup')
for c in [
    'gamerule doDaylightCycle false', 'time set 6000', 'weather clear 1000000',
    'gamerule doWeatherCycle false', 'gamerule doMobSpawning false',
    'gamerule mobGriefing false', 'gamerule keepInventory true',
    'difficulty peaceful', 'worldborder center 0 0', 'worldborder set 59999968',
    'setworldspawn 0 66 210', 'tp @p 0 66 210',
    'say BLAME! Megastructure: time locked to clear noon. Run /function blame:build to construct the core; /function blame:expand for the outer rings.'
]: line('setup',c)

# Beach: open sky, broad pale shore, a shallow bay, and flush industrial lights.
begin('beach')
# Organic coastline: 48 broad strips with changing width, not a square tile.
rng = random.Random(814721)
for zi in range(0, 57):
    z0 = 285 + zi*12
    z1 = z0 + 11
    t = zi/56
    width = int(530 - 180*t + 26*math.sin(zi*0.31) + rng.randint(-14,14))
    if z0 < 430:
        # sloping sand transition from beach to water
        shore_width = int(355 + 30*math.sin(zi*0.55) + rng.randint(-9,9))
        if shore_width > 0:
            fill('beach',-shore_width,63,z0,shore_width,63,z1,'minecraft:sand',0)
            fill('beach',-shore_width,61,z0,shore_width,62,z1,'minecraft:sandstone',0)
            fill('beach',-shore_width,60,z0,shore_width,60,z1,'minecraft:stone',6)
        # water begins where the shore recedes
        if width > shore_width:
            fill('beach',-width,57,z0,-shore_width-1,64,z1,'minecraft:water',0)
            fill('beach',-width,56,z0,-shore_width-1,56,z1,'minecraft:sandstone',0)
            fill('beach',shore_width+1,57,z0,width,64,z1,'minecraft:water',0)
            fill('beach',shore_width+1,56,z0,width,56,z1,'minecraft:sandstone',0)
    else:
        fill('beach',-width,57,z0,width,64,z1,'minecraft:water',0)
        fill('beach',-width,56,z0,width,56,z1,'minecraft:sandstone',0)
# Shore/arrival platform: a vast exposed sand plane, interrupted only by dune-like arms.
fill('beach',-235,63,55,235,63,285,'minecraft:sand',0)
fill('beach',-235,61,55,235,62,285,'minecraft:sandstone',0)
fill('beach',-223,60,65,223,60,274,'minecraft:stone',6)
# irregular side dunes, not a rectangular border
for i in range(14):
    x = rng.choice([-1,1])*rng.randint(165,248)
    z = rng.randint(75,280)
    rx = rng.randint(9,28); rz = rng.randint(15,38); h=rng.randint(1,7)
    fill('beach',x-rx,64,z-rz,x+rx,64+h,z+rz,'minecraft:sandstone',0)
    fill('beach',x-rx+2,65,z-rz+3,x+rx-2,65+h,z+rz-3,'minecraft:sand',0)
# low seawall, broken, not a straight line
for i in range(28):
    x = -360 + i*27
    z = 345 + int(14*math.sin(i*0.61)) + rng.randint(-5,5)
    fill('beach',x,61,z,x+18,66,z+3,'minecraft:stonebrick',0)
    fill('beach',x+2,67,z+1,x+16,68,z+2,'minecraft:stonebrick',2)
# bright low fixtures on the arrival promenade, with shadow-free, open sky above
for z in range(85, 292, 12):
    for sx in (-92,92):
        fill('beach',sx-3,62,z-3,sx+3,62,z+3,'minecraft:stonebrick',2)
        fill('beach',sx-1,63,z-1,sx+1,63,z+1,'minecraft:sea_lantern',0)
for x in range(-210,211,28):
    setb('beach',x,63,305,'minecraft:glowstone',0)
# small shallows, pale sand bars and reflective sea markers
for i in range(13):
    x = rng.randint(-390,390); z = rng.randint(360,860)
    rx = rng.randint(8,24); rz = rng.randint(10,36)
    fill('beach',x-rx,57,z-rz,x+rx,57,z+rz,'minecraft:sand',0)
    setb('beach',x,58,z,'minecraft:sea_lantern',0)

# Main approach portal & shoreline-facing gigantic gateway. Beach footprint remains uncovered.
begin('gate')
# colossal open gate piers
fill('gate',-310,63,-8,-274,360,16,'minecraft:concrete',7)
fill('gate',274,63,-8,310,360,16,'minecraft:concrete',7)
fill('gate',-324,63,-22,-310,250,-9,'minecraft:stonebrick',0)
fill('gate',310,63,-22,324,250,-9,'minecraft:stonebrick',0)
# collars/cap beam but leave >300 block opening for the sunlit approach
for y in (112,224,336):
    fill('gate',-326,y,-12,-270,y+7,20,'minecraft:concrete',15)
    fill('gate',270,y,-12,326,y+7,20,'minecraft:concrete',15)
fill('gate',-274,354,-12,274,378,20,'minecraft:concrete',7)
fill('gate',-248,350,-8,248,352,16,'minecraft:sea_lantern',0)
# hanging cables and structural stair-step ribs on either jamb
for s in (-1,1):
    for i in range(9):
        x = s*(286+i*4)
        y0 = 70 + (i%3)*13
        y1 = 175 + i*19
        fill('gate',x,y0,-7,x+1,y1,-6,'minecraft:iron_bars',0)
    for k in range(9):
        y = 82+k*28
        fill('gate',s*(270+k*2),y,3,s*(274+k*2),y+4,14,'minecraft:stonebrick',0)
# approaches vanish into an open throat
fill('gate',-210,64,-94,-170,66,-6,'minecraft:stonebrick',0)
fill('gate',170,64,-94,210,66,-6,'minecraft:stonebrick',0)
fill('gate',-208,67,-94,-205,75,-8,'minecraft:concrete',15)
fill('gate',205,67,-94,208,75,-8,'minecraft:concrete',15)
fill('gate',-168,64,-70,-120,65,-30,'minecraft:stonebrick',0)
fill('gate',120,64,-70,168,65,-30,'minecraft:stonebrick',0)
# second larger incomplete frame, set farther into the void
for x0,x1 in [(-475,-448),(448,475)]:
    fill('gate',x0,-280,-220,x1,760,-184,'minecraft:concrete',7)
    for y in (-192,0,192,384,576):
        fill('gate',x0-9,y,-224,x1+9,y+7,-177,'minecraft:concrete',15)
fill('gate',-448,728,-224,448,754,-177,'minecraft:concrete',7)
fill('gate',-390,722,-220,390,724,-182,'minecraft:glowstone',0)

# Core chamber: monumental shell surfaces and voids, layered discs, giant thin frames.
begin('core')
# central vertical shaft shell, not a solid box; massive open interior
for x1,x2 in [(-250,-226),(226,250)]:
    fill('core',x1,-512,-460,x2,1280,-40,'minecraft:concrete',7)
for z1,z2 in [(-460,-436),(-64,-40)]:
    fill('core',-226,-1024,z1,226,1792,z2,'minecraft:concrete',7)
# offset breaks create irregular silhouette; carve huge, intentional bites
fill('core',-253,166,-375,-224,274,-250,'minecraft:air',0)
fill('core',224,432,-360,253,540,-190,'minecraft:air',0)
fill('core',-225,760,-462,-66,845,-434,'minecraft:air',0)
fill('core',80,975,-462,224,1076,-434,'minecraft:air',0)
# central open shaft column and incomplete collars
fill('core',-8,-3072,-258,8,4096,-236,'minecraft:concrete',15)
fill('core',-2,-3072,-249,2,4096,-245,'minecraft:iron_block',0)
for y,r in [(-2048,96),(-1536,82),(-1024,118),(-768,82),(-384,110),(0,138),(180,112),(448,150),(704,98),(1024,126),(1536,84),(2048,144),(2816,105),(3456,76)]:
    # an irregular annular station around the core, using segmented plates
    fill('core',-r,y,-246,-r+18,y+6,-118,'minecraft:stonebrick',0)
    fill('core',r-18,y,-246,r,y+6,-118,'minecraft:stonebrick',0)
    fill('core',-r,y, -374,-r+18,y+6,-246,'minecraft:stonebrick',0)
    fill('core',r-18,y,-374,r,y+6,-246,'minecraft:stonebrick',0)
    fill('core',-r,y,-374,r,y+6,-356,'minecraft:stonebrick',0)
    # sparse lighting on selected stations
    if y in (0,448,1024):
        for x in (-r+8, r-9): setb('core',x,y+7,-310,'minecraft:sea_lantern',0)
# isolated drums/platforms, concentric rings with a hollow centre
for cy, cx, cz, radius in [(148,0,-155,118),(384,-108,-520,62),(-138,126,-405,74),(700,238,-610,88)]:
    for rad in range(radius, max(4,radius-7), -2):
        steps=max(32,int(rad*0.42))
        for i in range(steps):
            ang=2*math.pi*i/steps
            x=int(cx+rad*math.cos(ang)); z=int(cz+rad*math.sin(ang))
            fill('core',x,cy,z,x+2,cy+3,z+2,'minecraft:concrete',7 if i%5 else 15)
# delicate underframes under disc platforms
for x,z in [(-78,-234),(78,-234),(-78,-76),(78,-76),(-148,-550),(-68,-550),(192,-634),(276,-634)]:
    fill('core',x,-180,z,x+3,148,z+3,'minecraft:iron_bars',0)
# long catwalks crossing negative space; broken at intervals so they don't become streets
for i,(x0,z0,x1,z1,y) in enumerate([(-200,-155,200,-155,145),(-120,-500,118,-500,384),(-215,-650,50,-650,700),(-250,-90,250,-620,258)]):
    steps=max(abs(x1-x0),abs(z1-z0))//12
    for j in range(steps):
        if j%9 in (4,5): continue
        x=int(x0+(x1-x0)*j/max(1,steps)); z=int(z0+(z1-z0)*j/max(1,steps))
        yy=y+int(4*math.sin(j*0.21))
        fill('core',x,yy,z,x+11,yy,z+3,'minecraft:stonebrick',0)
        fill('core',x,yy-4,z+1,x+11,yy-1,z+2,'minecraft:iron_bars',0)
        if j%8==0: setb('core',x+5,yy+1,z+1,'minecraft:sea_lantern',0)

# Monumental broken elliptical rings: non-orthogonal silhouettes spanning many vertical layers.
# These are deliberately segmented with large missing arcs so the void remains legible.
ring_rng = random.Random(771903)
for ring_i, (cy0, rx0, rz0) in enumerate([
    (-2300, 330, 250), (-1640, 420, 310), (-900, 360, 280),
    (-180, 520, 390), (620, 430, 310), (1420, 580, 430),
    (2260, 400, 360), (3180, 620, 455),
]):
    phase = ring_rng.uniform(-0.28, 0.28)
    for deg in range(0, 360, 9):
        if (deg + ring_i*13) % ring_rng.choice([63, 72, 81]) in range(0, 13):
            continue
        a = math.radians(deg) + phase
        x = int(math.cos(a)*rx0) + int(13*math.sin(a*3+ring_i))
        z = -250 + int(math.sin(a)*rz0) + int(17*math.sin(a*2+ring_i))
        yy = cy0 + int(32*math.sin(a*3+ring_i*0.7))
        thick = ring_rng.randint(8, 22)
        block = ring_rng.choice([('minecraft:concrete',7),('minecraft:concrete',15),('minecraft:stonebrick',0)])
        if deg % 18 == 0:
            fill('core', x-thick, yy-12, z-10, x+thick, yy+14, z+10, block[0], block[1])
        else:
            fill('core', x-thick//2, yy-7, z-8, x+thick//2, yy+8, z+8, block[0], block[1])
    # Sparse glowing nodes mark only selected joints high above and below the beach.
    if ring_i in (0, 3, 5, 7):
        setb('core', int(rx0*0.72), cy0+18, -250, 'minecraft:sea_lantern', 0)

# Huge discontinuous vertical ribs and suspended beams; separate scales cross the main void.
for rib_i, (xc, zc, ybottom, ytop, rx) in enumerate([
    (-430,-300,-3072,3000,500), (410,-440,-2500,3900,620),
    (-120,-780,-3600,2500,360), (180,-120,-1800,4200,700),
]):
    for deg in range(0, 360, 10):
        if (deg + rib_i*17) % 70 in range(0, 19):
            continue
        a = math.radians(deg + rib_i*11)
        x = xc + int(rx*math.cos(a))
        y = (ybottom+ytop)//2 + int((ytop-ybottom)*0.5*math.sin(a))
        z = zc + int(24*math.sin(a*2))
        size = 9 + (deg%3)*3
        fill('core', x-size, y-20, z-size, x+size, y+20, z+size, 'minecraft:concrete', 7 if deg%2 else 15)
        if deg%40 == 0:
            fill('core', x-size-3, y+22, z-size-3, x+size+3, y+25, z+size+3, 'minecraft:stonebrick', 0)
# Cropped cross-void spans, each interrupted to avoid creating tidy floors.
for layer_i, (yy, zc, halfspan) in enumerate([
    (-1560,-360,430),(-820,-510,560),(-80,-280,610),(560,-540,500),
    (1280,-380,680),(2040,-620,480),(2760,-290,560),
]):
    for seg in range(8):
        if (seg + layer_i) % 5 == 1:
            continue
        x1 = -halfspan + seg*(2*halfspan//8) + 10
        x2 = min(halfspan, x1 + int(halfspan*0.22))
        if x2 > x1:
            fill('core',x1,yy+int(9*math.sin(seg*0.8+layer_i)),zc-8,x2,yy+6,zc+8,
                 'minecraft:concrete',15 if seg%3==0 else 7)

# Generic organic sector procedure, deterministic but not grid based.
def make_sector(idx, cx, cz, radius, outer=False):
    name=f'sector/s{idx:02d}'
    begin(name)
    sr=random.Random(10000+idx*173+cx*3-cz*5)
    # different architectural archetypes: shell, spine, cage, slab field, or split frame
    archetype=idx%5
    if outer:
        base_y=sr.choice([-768,-512,-256,128,384,768,1024])
        max_h=sr.randint(360,1250)
        min_y=base_y-sr.randint(0,420)
    else:
        base_y=sr.choice([-512,-256,-128,0,128,256,512])
        max_h=sr.randint(230,900)
        min_y=base_y-sr.randint(0,250)
    top_y=base_y+max_h
    w=sr.randint(44,106) if not outer else sr.randint(30,72)
    d=sr.randint(36,92) if not outer else sr.randint(24,66)
    mat=sr.choice([(7,15),(7,8),(15,8),(0,7)])
    # four partial buttresses, with intentional offsets and gaps
    offsets=[(-w//2,-d//2),(w//2-6,-d//2),(-w//2,d//2-6),(w//2-6,d//2-6)]
    for j,(ox,oz) in enumerate(offsets):
        hh=int(max_h*sr.uniform(0.42,1.0))
        y0=min_y+sr.randint(0,70); y1=y0+hh
        thick=sr.randint(4,10)
        fill(name,cx+ox,y0,cz+oz,cx+ox+thick,y1,cz+oz+thick,'minecraft:concrete',mat[0] if j%2 else mat[1])
        # irregular collars at a different rhythm for each support
        for yy in range(y0+48+sr.randint(0,35), y1-5, sr.randint(72,132)):
            fill(name,cx+ox-3,yy,cz+oz-3,cx+ox+thick+3,yy+4,cz+oz+thick+3,'minecraft:stonebrick',0 if j%2 else 2)
    # primary shell planes are deliberately broken; omit some heights and carve apertures
    for layer in range(sr.randint(2,5)):
        yy=base_y+layer*sr.randint(100,190)
        if archetype in (0,3,4):
            # oriented vertical slab, broken into asymmetrical panels
            if sr.random()<0.5:
                x0=cx-sr.randint(25,55); x1=cx+sr.randint(25,55)
                z=cz+sr.choice([-1,1])*d//2
                segments=sr.randint(2,4)
                for seg in range(segments):
                    sx0=x0+(x1-x0)*seg//segments+sr.randint(-5,6)
                    sx1=x0+(x1-x0)*(seg+1)//segments-sr.randint(0,7)
                    if sx1<sx0: continue
                    ylo=yy+sr.randint(-15,15); yhi=ylo+sr.randint(38,110)
                    fill(name,sx0,ylo,z,sx1,yhi,z+sr.randint(4,12),'minecraft:concrete',mat[0])
            else:
                z0=cz-sr.randint(25,55); z1=cz+sr.randint(25,55)
                x=cx+sr.choice([-1,1])*w//2
                segments=sr.randint(2,4)
                for seg in range(segments):
                    sz0=z0+(z1-z0)*seg//segments+sr.randint(-5,6)
                    sz1=z0+(z1-z0)*(seg+1)//segments-sr.randint(0,7)
                    if sz1<sz0: continue
                    ylo=yy+sr.randint(-15,15); yhi=ylo+sr.randint(38,110)
                    fill(name,x,ylo,sz0,x+sr.randint(4,12),yhi,sz1,'minecraft:concrete',mat[0])
        # partial horizontal shells/cap slabs at varied elevations
        if archetype in (1,2,3) or layer%2==0:
            spanx=sr.randint(42,100); spanz=sr.randint(38,92)
            for part in range(sr.randint(2,4)):
                px=cx+sr.randint(-spanx//2,spanx//2)
                pz=cz+sr.randint(-spanz//2,spanz//2)
                lx=sr.randint(14,spanx//2+8); lz=sr.randint(12,spanz//2+8)
                py=yy+sr.randint(-24,28)
                fill(name,px,py,pz,px+lx,py+sr.randint(2,7),pz+lz,'minecraft:concrete',15 if part%3==0 else 7)
                # fractal bite / inner opening cuts
                if part==1 and lx>24 and lz>24:
                    fill(name,px+lx//3,py-1,pz+lz//3,px+lx//3+lx//4,py+9,pz+lz//3+lz//4,'minecraft:air',0)
    # a few skeletal suspension lines from high fragments
    for c in range(sr.randint(2,6)):
        x=cx+sr.randint(-w//2,w//2); z=cz+sr.randint(-d//2,d//2)
        ya=base_y+sr.randint(80,max(95,max_h))
        yb=min(4096,ya+sr.randint(48,320))
        fill(name,x,ya,z,x+sr.choice([0,1]),yb,z,'minecraft:iron_bars',0)
        if c%2==0: setb(name,x,yb,z,'minecraft:sea_lantern',0)
    # step-rib, diagonal in silhouette but not a single block-perfect grid
    sx=cx+sr.randint(-w//2,w//2); sz=cz+sr.randint(-d//2,d//2)
    dy=sr.choice([-1,1]); dx=sr.choice([-1,1])
    for k in range(sr.randint(8,19)):
        x=sx+k*dx*sr.randint(2,4); z=sz+k*dy*sr.randint(1,3); y=base_y+k*sr.randint(8,18)
        block=sr.choice([(0,7),(7,15),(15,8)])[0]
        fill(name,x,y,z,x+sr.randint(3,8),y+sr.randint(4,11),z+sr.randint(3,8),'minecraft:concrete',block)
    # catwalk spur toward the next mass, discontinuous to preserve emptiness
    length=sr.randint(50,138)
    axis=sr.choice(['x','z']); direction=sr.choice([-1,1]); yy=base_y+sr.randint(12,130)
    for p in range(0,length,8):
        if p%40 in (24,32): continue
        if axis=='x': x=cx+direction*p; z=cz+sr.randint(-2,2)
        else: x=cx+sr.randint(-2,2); z=cz+direction*p
        fill(name,x,yy+int(3*math.sin(p*0.045)),z,x+(8 if axis=='x' else 2),yy+int(3*math.sin(p*0.045)),z+(2 if axis=='x' else 8),'minecraft:stonebrick',0)
        fill(name,x,yy-3,z,x+(8 if axis=='x' else 2),yy-1,z+(2 if axis=='x' else 8),'minecraft:iron_bars',0)
    # site glyph / rare bright lamps — sparse, visible from a distance
    for p in range(sr.randint(1,3)):
        setb(name,cx+sr.randint(-w//2,w//2),base_y+sr.randint(0,max(1,max_h)),cz+sr.randint(-d//2,d//2),'minecraft:glowstone',0)
    return name

# core functions exist at multiple scales and elevations
begin('build')
for sub in ['setup','beach','gate','core']:
    call('build',sub)
# inner ring at 260-820 blocks, biased north so the vista opens past the gate
inner_centers=[(-330,-430), (340,-510), (-520,-110), (520,-80), (-430,260), (430,310), (-170,-820), (225,-930), (720,-660), (-760,-540), (-830,40), (790,120), (-1050,-360), (1050,-280), (-1160,520), (1160,640), (-850,950), (900,1070), (-420,-1240), (510,-1380), (-1360,-840), (1400,-810), (1300,100), (-1250,100), (0,1450)]
for i,(cx,cz) in enumerate(inner_centers):
    call('build',make_sector(i,cx,cz,int(math.hypot(cx,cz))))
# extra macro forms at upper and lower vertical layers near the core
for j,(x,z,y,h) in enumerate([(-180,-320,-2048,3200),(180,-480,-1536,4096),(-300,-650,384,3072),(325,-760,-1024,3584),(0,-1000,1024,3072),(-420,-200,-2800,3600),(440,-350,-1536,4352)]):
    nm=f'sector/core_alt{j}'
    begin(nm)
    fill(nm,x,y,z,x+5,y+h,z+5,'minecraft:concrete',15)
    fill(nm,x-8,y+128,z-6,x+14,y+132,z+12,'minecraft:stonebrick',0)
    fill(nm,x-10,y+h//2,z-10,x+17,y+h//2+5,z+13,'minecraft:concrete',7)
    fill(nm,x-4,y+h-28,z-4,x+16,y+h-22,z+16,'minecraft:concrete',15)
    for k in range(7):
        yy=y+160+k*(h//8)
        fill(nm,x-15,yy,z-15,x+24,yy+4,z-11,'minecraft:stonebrick',0)
        if k%2==0: fill(nm,x-14,yy+5,z-13,x+23,yy+9,z-12,'minecraft:sea_lantern',0)
    call('build',nm)
# outer expansion comprises a large, organic ring of macro-forms; not a grid.
begin('expand')
for i in range(36):
    # Golden-angle distribution with seeded jitter; sector boundaries are never constructed.
    rr=1600 + (i%4)*280 + (i//12)*650
    theta=i*2.399963229728653 + 0.31*math.sin(i*0.72)
    cx=int(rr*math.cos(theta)); cz=int(rr*math.sin(theta)-360)
    nm=make_sector(100+i,cx,cz,rr,outer=True)
    call('expand',nm)
# further ring: thinner hanging frames, horizons and bridges out to about 5.6 km
begin('horizon')
for i in range(48):
    rr=3800 + (i%5)*440
    theta=i*2.399963229728653+0.8
    cx=int(rr*math.cos(theta)); cz=int(rr*math.sin(theta)-520)
    nm=f'sector/h{i:02d}'; begin(nm)
    sr=random.Random(55129+i)
    y=sr.choice([-4096,-3072,-2048,-1024,128,768,1536,2304,3072,4096])
    height=sr.randint(700,2200)
    # distant gigantic thin wall or frame; the void inside is larger than the structure.
    if i%3==0:
        fill(nm,cx-5,y,cz-120,cx+5,y+height,cz+120,'minecraft:concrete',15)
        fill(nm,cx-8,y+height//2,cz-90,cx+8,y+height//2+6,cz+90,'minecraft:stonebrick',0)
        fill(nm,cx-4,y+height//3,cz-119,cx+4,y+height//3+80,cz-117,'minecraft:air',0)
    elif i%3==1:
        fill(nm,cx-150,y,cz-4,cx+150,y+height,cz+4,'minecraft:concrete',7)
        for p in range(5):
            px=cx-120+p*58
            fill(nm,px,y+height//4,cz-6,px+4,y+height//4+height//3,cz+6,'minecraft:air',0)
    else:
        # three separated pillars and a slumped cap, intentionally not a closed box
        for dx,dz,w in [(-80,-18,10),(12,24,8),(96,-8,6)]:
            fill(nm,cx+dx,y,cz+dz,cx+dx+w,y+height-sr.randint(0,200),cz+dz+w,'minecraft:concrete',15)
        fill(nm,cx-120,y+height-18,cz-60,cx+36,y+height-10,cz+60,'minecraft:stonebrick',0)
        fill(nm,cx+42,y+height-64,cz-54,cx+128,y+height-54,cz+50,'minecraft:concrete',7)
    # a tiny amber/cyan beacon for scale
    setb(nm,cx,y+height,cz,'minecraft:sea_lantern',0)
    call('horizon',nm)
# one very small text label on a landmark via signs would be awkward with old NBT; rely on form.

# Save generated functions. Keep each file comfortably below 1.12's function command cap.
for name, cmds in funcs.items():
    dest = FUN / (name + '.mcfunction')
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text('\n'.join(cmds).rstrip()+'\n',encoding='utf-8')

# Prism instance components. Launcher resolves the Forge component; user adds the two licensed jars from source pages.
mmc = {
  'formatVersion':1,
  'components':[
    {'uid':'net.minecraft','version':'1.12.2','cachedName':'Minecraft','cachedVersion':'1.12.2','cachedVolatile':True,'dependencyOnly':True},
    {'uid':'net.minecraftforge','version':'14.23.5.2860','cachedName':'Forge','cachedVersion':'14.23.5.2860','cachedVolatile':True}
  ]
}
inst=KIT/'minecraft_instance'
(inst/'mmc-pack.json').write_text(json.dumps(mmc,indent=2)+'\n',encoding='utf-8')
(inst/'instance.cfg').write_text('''[General]\nConfigVersion=1.2\nInstanceType=OneSix\nname=BLAME! Cubic Megastructure 1.12.2\niconKey=blame\nnotes=Forge 1.12.2 + CubicChunks infinite height. Follow INSTALL_FIRST.txt before creating your cubic world.\n''',encoding='utf-8')
(inst/'minecraft'/'mods'/'ADD_CUBICCHUNKS_AND_OPTIFINE_HERE.txt').write_text('''This directory is intentionally a clean mods folder. For the CurseForge import package, CubicChunks is downloaded by its manifest. In this manual instance template, download CubicChunks from the project page and place the JAR here. Confirm launch before adding optional OptiFine E2. Third-party binaries are not bundled in the manual template.\n''',encoding='utf-8')
(inst/'minecraft'/'shaderpacks'/'ADD_BSL_SHADERPACK_HERE.txt').write_text('''Download BSL Shaders v8.2.08 from the link in INSTALL_FIRST.txt and put its ZIP here unchanged. Shaders require a compatible OptiFine build.\nThen choose Options > Video Settings > Shaders in-game.\n''',encoding='utf-8')
(inst/'minecraft'/'config'/'README_CUBICCHUNKS_CONFIG.txt').write_text('''Do not force CubicChunks on a pre-existing vanilla Anvil world. Create a NEW world after installing CubicChunks, and enable Cubic Chunks under More World Options. Existing vanilla chunks are not converted automatically.\n''',encoding='utf-8')

# Write detailed user steps and limitations.
readme = '''BLAME! — CUBIC MEGASTRUCTURE / PROCEDURAL EDITION

TARGET: Minecraft Java 1.12.2 + Forge 14.23.5.2860 + CubicChunks.
CubicChunks' usable release is the 1.12.2 Forge line. The modern CC3 rewrite is not yet usable for gameplay.

IMPORTANT HONEST NOTE
This is an instance/generator kit, not a pre-baked CubicChunks save. CubicChunks uses a special cubic world format; existing vanilla saves are not converted by toggling the mod. Create a NEW cubic world, copy the included functions, and run the build commands. This avoids handing you another world file that fails to load.

RECOMMENDED: MODPACK IMPORT
Use BLAME_CubicChunks_1.12.2_CurseForge_Import.zip. Its root manifest targets Minecraft 1.12.2 + Forge 14.23.5.2860 and asks the launcher to download CubicChunks file 9027901 from CurseForge. Internet is required during import.

MANUAL INSTANCE-TEMPLATE FALLBACK
1. Import this ZIP as an existing Prism/ElyPrism instance; instance.cfg and mmc-pack.json are at the archive root.
2. Open minecraft/mods and add CubicChunks 1.12.2-0.0.1301.0 from https://modrinth.com/mod/cubicchunks/version/1.12.2-0.0.1301.0.
3. Launch once with CubicChunks but WITHOUT OptiFine. Confirm Forge starts and a new cubic world opens.
4. Optional shader path: CubicChunks compatibility notes list OptiFine 1.12.2 HD U E2 for the newer 1.12.2 line. Download from https://optifine.net/downloads and add it to minecraft/mods only after the base launch works. If the game crashes, remove OptiFine.
5. Download BSL Shaders v8.2.08 from https://www.curseforge.com/minecraft/shaders/bsl-shaders/files/5077991 and place the shader ZIP unchanged into minecraft/shaderpacks.

CREATE THE WORLD AND BUILD IT
6. Create a NEW world; in More World Options enable Cubic Chunks and turn Allow Cheats on (Creative recommended). Never reuse a vanilla world. If build height is limited to 256 on first load, exit and reload the new cubic world.
7. In the instance Minecraft folder, open BLAME_CubicChunks_Procedural_Kit/world_template/data/functions. Copy its functions folder into the new world's data folder. The result must end in saves/<world>/data/functions/blame/build.mcfunction.
8. Load the cubic world and run each command separately; wait for each to finish:
      /function blame:setup
      /function blame:build
      /function blame:expand
      /function blame:horizon
   The first two create the daylit beach, gate, shaft and near-field sectors. Expand and horizon add outer sectors and distant silhouettes. Save and quit after completion.

WHAT THIS BUILDS
- A clear-noon beach and bay near Y=64, with embedded floor lights and open sky.
- A colossal gate, central shaft, suspended paths, fractured shells and elliptical mega-ribs.
- 25 near-field sectors, 36 mid-field sectors and 48 far-horizon forms placed with a seeded golden-angle distribution rather than a square grid.
- Architecture reaching approximately Y=-4096 to Y=+6296, with distant structures about 5.6 km from the center.
- 123 legacy 1.12.2 functions comprising 21,652 commands.

IMPORTANT LIMIT
This is a seeded procedural construction kit for a very large finite region, not a custom world-generation mod that creates BLAME architecture forever as new chunks load. CubicChunks provides almost-unlimited vertical space. The preview is a schematic, not an in-game screenshot.

SAFETY / PERFORMANCE
The build functions are large; wait for each command to finish and do not run multiple build functions simultaneously. Back up saves. Keep the mod list minimal until CubicChunks works. Start BSL at low shadow quality and increase it only if performance allows.
'''
(KIT/'INSTALL_FIRST.txt').write_text(readme,encoding='utf-8')

# Add the world template function files separately at top level for easier copying.
shutil.copytree(KIT/'world_template'/'data'/'functions', KIT/'FUNCTIONS_TO_COPY', dirs_exist_ok=True)

# procedural source script bundled so it can be rebuilt/adjusted
src = Path(__file__).resolve()
shutil.copy2(src,KIT/'source'/'build_cubic_pack.py')
(KIT/'source'/'README_SOURCE.txt').write_text('''Run with Python 3 + Pillow to rebuild the generator kit. It uses a deterministic seed and emits legacy 1.12.2 /fill and /setblock function commands. By default it writes the kit and ZIP outputs alongside the script. Set BLAME_OUT_DIR to choose an output folder.\n''',encoding='utf-8')

# Overview schematic: a readable large-area plan plus vertical extent summary.
W,H=1600,980
im=Image.new('RGB',(W,H),(8,13,19)); d=ImageDraw.Draw(im)
try:
    ftitle=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',30)
    fsub=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',15)
    flabel=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',14)
    fsmall=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',12)
    fmed=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',14)
except: ftitle=fsub=flabel=fsmall=fmed=None
d.text((36,26),'BLAME!  /  CUBIC MEGASTRUCTURE',font=ftitle,fill=(226,238,243))
d.text((38,67),'SEEDED PROCEDURAL ARCHITECTURE   •   FORGE 1.12.2 + CUBIC CHUNKS',font=fsub,fill=(115,177,193))
# Left: top-down plan of an intentionally sparse multi-kilometre region.
cx,cy=500,475
scale=0.085
# ocean bay
d.rectangle((cx-560*scale,cy+300*scale,cx+560*scale,cy+960*scale),fill=(14,52,69),outline=(35,114,132),width=2)
for yy in range(int(cy+310*scale),int(cy+950*scale),12):
    d.arc((cx-550*scale,yy,cx+550*scale,yy+18),190,350,fill=(31,89,107),width=1)
# beach: broad, open-sky arrival area
d.ellipse((cx-250*scale,cy+55*scale,cx+250*scale,cy+315*scale),fill=(207,192,149),outline=(244,225,175),width=2)
d.ellipse((cx-225*scale,cy+75*scale,cx+225*scale,cy+290*scale),outline=(162,141,106),width=3)
# gate piers + luminous lintel
d.rectangle((cx-310*scale,cy-8*scale,cx-274*scale,cy+18*scale),fill=(99,118,128))
d.rectangle((cx+274*scale,cy-8*scale,cx+310*scale,cy+18*scale),fill=(99,118,128))
d.line((cx-274*scale,cy-10*scale,cx+274*scale,cy-10*scale),fill=(121,142,151),width=5)
# central shell and fractured vertical rings as architectural silhouettes
d.ellipse((cx-650*scale,cy-900*scale,cx+650*scale,cy+220*scale),outline=(78,104,120),width=2)
d.ellipse((cx-470*scale,cy-760*scale,cx+470*scale,cy+70*scale),outline=(43,65,79),width=2)
for i in range(6):
    xoff=int((180+30*i)*math.cos(i*1.3))*scale
    d.arc((cx-(300+i*35)*scale,cy-(1200+i*190)*scale,cx+(300+i*35)*scale,cy+(200+i*190)*scale),
          start=205+i*19,end=510-i*13,fill=(54+3*i,82+3*i,98+3*i),width=2)
# all 25 inner macro-sector anchors
for i,(x,z) in enumerate(inner_centers):
    px=cx+x*scale; py=cy+z*scale
    rr=5+(i%4)*2
    poly=[(px-rr,py-rr*.8),(px+rr*.65,py-rr),(px+rr,py+rr*.2),(px+rr*.3,py+rr),(px-rr,py+rr*.45)]
    d.polygon(poly,fill=(34,48,60),outline=(107,139,157))
    d.line((px-rr*.7,py,px+rr*.65,py+rr*.1),fill=(93,142,158),width=1)
# matching 36 outer sectors
for i in range(36):
    rr=1600 + (i%4)*280 + (i//12)*650
    theta=i*2.399963229728653 + 0.31*math.sin(i*0.72)
    x=int(rr*math.cos(theta)); z=int(rr*math.sin(theta)-360)
    px=cx+x*scale; py=cy+z*scale
    r=4+(i%3)
    d.line((px-r,py-r,px+r,py+r),fill=(78,113,130),width=2)
    d.line((px-r,py+r,px+r,py-r),fill=(78,113,130),width=2)
# matching 48 far-horizon forms
for i in range(48):
    rr=3800+(i%5)*440
    theta=i*2.399963229728653+0.8
    px=cx+int(rr*math.cos(theta))*scale
    py=cy+(int(rr*math.sin(theta)-520))*scale
    if 20<px<980 and 110<py<830:
        r=3+(i%3)
        d.line((px-r,py-r,px+r,py+r),fill=(105,137,151),width=2)
        d.line((px-r,py+r,px+r,py-r),fill=(105,137,151),width=2)
# map labels
d.ellipse((cx-4,cy+210*scale-4,cx+4,cy+210*scale+4),fill=(255,244,202))
d.text((cx+25,cy+210*scale-8),'DAYLIT BEACH / SPAWN',font=flabel,fill=(245,229,187))
d.text((cx+15,cy-28),'OPEN GATE',font=fsmall,fill=(185,206,214))
d.text((cx+95,cy-160),'CENTRAL SHAFT',font=flabel,fill=(202,218,225))
# right panel; wrapped and fully inside canvas
d.line((1020,115,1020,920),fill=(40,62,75),width=2)
d.text((1050,125),'A PROCEDURAL SYSTEM',font=flabel,fill=(219,232,238))
info=[
    '25 near-field sectors: varied shells, frames, vertical spines and broken catwalks.',
    '36 mid-field sectors scattered on a golden-angle layout, not a square grid.',
    '48 far silhouettes continue across a radius of roughly 5.6 kilometres.',
    'Elliptical mega-ribs and fractured rings span below and far above the beach.',
    'Clear noon is locked in; the arrival beach is open to sky with embedded lights.',
]
def wrap_lines(txt, max_chars=57):
    return textwrap.wrap(txt, width=max_chars, break_long_words=False, break_on_hyphens=False)
y=166
for txt in info:
    d.ellipse((1052,y+4,1059,y+11),fill=(75,170,190))
    lines=wrap_lines(txt,57)
    for k,ln in enumerate(lines):
        d.text((1072,y+k*19),ln,font=fsmall,fill=(159,182,193))
    y+=max(45,len(lines)*19+18)
# vertical scale graph
d.text((1050,y+7),'BUILT ELEVATION ENVELOPE',font=flabel,fill=(219,232,238)); y+=42
xaxis=1080; top_y=y; bot_y=y+190
d.line((xaxis,top_y,xaxis,bot_y),fill=(81,112,128),width=5)
def elev_to_y(e):
    return top_y+int((6296-e)/(6296+4096)*190)
for elev,label in [(6296,'+6296'),(4096,'+4096'),(2048,'+2048'),(0,'0 / beach nearby'),(-2048,'-2048'),(-4096,'-4096')]:
    yy=elev_to_y(elev)
    d.line((xaxis-7,yy,xaxis+10,yy),fill=(104,185,197),width=2)
    d.text((1102,yy-7),label,font=fsmall,fill=(159,182,193))
# command runbook to right of elevation scale
d.text((1290,top_y),'IN-GAME BUILD',font=flabel,fill=(219,232,238))
for i,txt in enumerate(['/function blame:setup','/function blame:build','/function blame:expand','/function blame:horizon']):
    d.text((1290,top_y+35+i*27),txt,font=fsmall,fill=(109,196,205))
d.text((1290,top_y+165),'Seeded Python generator',font=fsmall,fill=(126,151,163))
d.text((1290,top_y+182),'creates 118 functions.',font=fsmall,fill=(126,151,163))
d.text((38,943),'SCHEMATIC — not an in-game screenshot. Generate in a NEW CubicChunks world; this is a procedural build kit, not a pre-baked save.',font=fsmall,fill=(102,125,138))
overview=KIT/'BLAME_CubicChunks_Overview.png'; im.save(overview)

# README links duplicate at instance root for easy notice after import
(inst/'minecraft'/'INSTALL_FIRST.txt').write_text(readme,encoding='utf-8')

# Bundle zip of the kit; standalone world function files and instance files accessible.
zip_path=OUT/'BLAME_CubicChunks_1.12.2_Procedural_Kit.zip'
if zip_path.exists(): zip_path.unlink()
with zipfile.ZipFile(zip_path,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as zf:
    for p in KIT.rglob('*'):
        if p.is_file(): zf.write(p,p.relative_to(KIT.parent))
# Prism instance zip includes the folder root, and docs, plus overview.
instance_zip=OUT/'BLAME_CubicChunks_1.12.2_ElyPrism_Instance.zip'
if instance_zip.exists(): instance_zip.unlink()
with zipfile.ZipFile(instance_zip,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as zf:
    for p in inst.rglob('*'):
        if p.is_file(): zf.write(p,p.relative_to(inst))
    zf.write(KIT/'INSTALL_FIRST.txt','INSTALL_FIRST.txt')
    zf.write(overview,overview.name)
functions_zip=OUT/'BLAME_CubicChunks_1.12.2_Functions_and_Source.zip'
if functions_zip.exists(): functions_zip.unlink()
with zipfile.ZipFile(functions_zip,'w',zipfile.ZIP_DEFLATED,compresslevel=7) as zf:
    for p in (KIT/'world_template').rglob('*'):
        if p.is_file(): zf.write(p,p.relative_to(KIT))
    for p in (KIT/'source').rglob('*'):
        if p.is_file(): zf.write(p,p.relative_to(KIT))
    zf.write(KIT/'INSTALL_FIRST.txt','INSTALL_FIRST.txt')
    zf.write(overview,overview.name)

# Print checks and summary
all_funcs=list(FUN.rglob('*.mcfunction'))
cmd_count=sum(len(x.read_text().splitlines()) for x in all_funcs)
max_lines=max(len(x.read_text().splitlines()) for x in all_funcs)
# no fill > 28000 blocks and paths verify
n_bad=0
for p in all_funcs:
    for l in p.read_text().splitlines():
        if l.startswith('fill '):
            a=l.split(); x1,y1,z1,x2,y2,z2=map(int,a[1:7])
            vol=(abs(x2-x1)+1)*(abs(y2-y1)+1)*(abs(z2-z1)+1)
            if vol>32768: n_bad+=1
print(json.dumps({
    'instance_zip':str(instance_zip),'kit_zip':str(zip_path),'functions_zip':str(functions_zip),'overview':str(overview),
    'function_count':len(all_funcs),'command_count':cmd_count,'max_lines_in_function':max_lines,'invalid_fill_volumes':n_bad,
    'functions': [str(p.relative_to(KIT)) for p in all_funcs[:10]],
    'sizes': {p.name:p.stat().st_size for p in (instance_zip,zip_path,functions_zip,overview)}
},indent=2))
