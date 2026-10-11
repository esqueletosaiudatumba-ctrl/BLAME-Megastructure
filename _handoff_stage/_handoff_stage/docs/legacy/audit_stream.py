#!/usr/bin/env python3
"""File-level audit for the active BLAME! CubicChunks streaming functions.
Run from the instance root: python project_source/tools/audit_stream.py
This is a validator, not an in-game compatibility test.
"""
from pathlib import Path
import collections, json, re, sys
ROOT = Path(__file__).resolve().parents[2]
SAVE = ROOT / 'minecraft' / 'saves' / 'BLAME_Infinite_Megastructure'
FUN = SAVE / 'data' / 'functions' / 'blame'
STREAM = FUN / 'stream'
DISPATCH = FUN / 'dispatch'
issues=[]
stream_files=sorted(STREAM.glob('*.mcfunction'))
stream_by_name={p.stem:p for p in stream_files}
refs=[]
for p in sorted(DISPATCH.glob('*.mcfunction')):
    for n,line in enumerate(p.read_text().splitlines(),1):
        m=re.search(r'function blame:stream/([A-Za-z0-9_]+)',line)
        if m: refs.append((m.group(1),p.name,n,line))
ref_names={x[0] for x in refs}
missing=sorted(ref_names-set(stream_by_name))
unreferenced=sorted(set(stream_by_name)-ref_names)
if missing: issues.append(f'{len(missing)} dispatch references missing stream functions')
if unreferenced: issues.append(f'{len(unreferenced)} stream functions have no dispatcher reference')
refs_by_name=collections.defaultdict(list)
for r in refs: refs_by_name[r[0]].append(r)
markers={}; command_counts=collections.Counter(); fill_count=0; setblock_count=0; max_fill=0; min_y=10**20; max_y=-(10**20); max_line=0
for name,p in stream_by_name.items():
    lines=[x.strip() for x in p.read_text().splitlines() if x.strip()]
    command_counts[len(lines)] += 1
    max_line=max([max_line]+[len(x) for x in lines])
    if not lines:
        issues.append(f'{p.name} is empty'); continue
    mm=re.fullmatch(r'setblock (-?\d+) (-?\d+) (-?\d+) minecraft:structure_void 0',lines[-1])
    if not mm:
        issues.append(f'{p.name}: final command is not the expected structure_void sentinel'); continue
    marker=tuple(map(int,mm.groups()))
    if marker in markers: issues.append(f'{p.name}: sentinel duplicates {markers[marker]}')
    markers[marker]=p.name
    # The marker is deliberately outside the visible geometry; it must match detect exactly.
    for _,df,ln,line in refs_by_name.get(name,[]):
        dm=re.search(r'detect (-?\d+) (-?\d+) (-?\d+) minecraft:air 0 function',line)
        if not dm or tuple(map(int,dm.groups())) != marker:
            issues.append(f'{df}:{ln}: detect coordinate does not match {p.name} sentinel')
    for i,line in enumerate(lines,1):
        if line.startswith('fill '):
            fill_count+=1
            m=re.match(r'fill (-?\d+) (-?\d+) (-?\d+) (-?\d+) (-?\d+) (-?\d+) (\S+)',line)
            if not m:
                issues.append(f'{p.name}:{i}: unparsed fill command'); continue
            x1,y1,z1,x2,y2,z2=map(int,m.groups()[:6])
            vol=(abs(x2-x1)+1)*(abs(y2-y1)+1)*(abs(z2-z1)+1)
            max_fill=max(max_fill,vol)
            min_y=min(min_y,y1,y2); max_y=max(max_y,y1,y2)
            if vol>32768: issues.append(f'{p.name}:{i}: fill volume {vol} exceeds vanilla 1.12.2 command limit')
        elif line.startswith('setblock '): setblock_count+=1
        elif not (line.startswith('fill ') or line.startswith('setblock ')):
            issues.append(f'{p.name}:{i}: unexpected command type: {line[:40]}')
# Every stream target must be represented by exactly one detector, to avoid starvation or duplicate scheduling.
counts=collections.Counter(r[0] for r in refs)
for name,count in counts.items():
    if count != 1: issues.append(f'{name}: {count} dispatcher references (expected 1)')
result={
 'project':'BLAME! Infinite Megastructure — file-level audit',
 'stream_functions':len(stream_files),'dispatcher_functions':len(list(DISPATCH.glob('*.mcfunction'))),
 'dispatcher_references':len(refs),'unique_sentinels':len(markers),
 'fill_commands':fill_count,'setblock_commands':setblock_count,
 'max_fill_volume':max_fill,'fill_y_min':min_y if fill_count else None,'fill_y_max':max_y if fill_count else None,
 'max_command_line_chars':max_line,'issues':issues,
 'runtime_tested':False
}
print(json.dumps(result,indent=2))
sys.exit(1 if issues else 0)
