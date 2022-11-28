import os
import re
import subprocess


def do(file):
    print(file)
    cmd = "java", "-ea", "a.java", file, r"C:\aklo\aklo"
    p = subprocess.Popen(
        cmd,
        stderr=subprocess.PIPE,
    )
    stdout, stderr = p.communicate()
    stderr = str(stderr, "utf-8")
    for s in open(file).readlines():
        s = s.strip()
        m = re.match(r";\s*ERR\s+(.*)", s)
        if m:
            if m[1] in stderr:
                return
            raise Exception(stderr)
    if stderr:
        raise Exception(stderr)
    if p.returncode:
        raise Exception(str(p.returncode))


here = os.path.dirname(os.path.realpath(__file__))
for root, dirs, files in os.walk(here):
    for file in files:
        ext = os.path.splitext(file)[1]
        if ext == ".k":
            do(os.path.join(root, file))
print("ok")
