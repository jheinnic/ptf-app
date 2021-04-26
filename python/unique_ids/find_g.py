import itertools 
import math

def find_g_for_p(p):
     m = 0
     w = 0
     retval = []
     for ii in range(p-1, 0, -1):
         x = 1
         g = ii
         v = set()
         v.add(1)
         z = 0
         while z == 0:
             # print(f" ** {x}")
             x = (x * g) % p
             z = x in v
             v.add(x)
         b = len(v)
         # print(f"{ii} -> {b}")
         if b == (p - 1):
             retval.append(ii)
     return retval

 
def wind_g_for_p(g, p):
    x = 1
    v = set()
    l = list()
    v.add(x)
    l.append(x)
    for ii in range(0, p):
        # print(x)
        x = (x * g) % p
        y = set()
        y.add(x)
        z = len(v.intersection(y))
        if z > 0:
            print(f"Cycle ends after {ii} at {x}")
            return v
        v.add(x)
        l.append(x)
        if (ii % 5000) == 4999:
            print(f"Found {ii}\n")
    return(l)
 

def test_g_for_p(g, p):
    x = 1
    v = set()
    v.add(x)
    for ii in range(0, p):
        print(x)
        x = (x * g) % p
        y = set()
        y.add(x)
        if len(v.intersection(y)) > 0:
            print(f"Cycle found after {len(v)} iterations, halting on {ii}")
            return v
        v.add(x)
        if (ii % 1000) == 999:
            print(f"So far, len(v) = {len(v)}")
    print(x)
    return(v)


def pick_a_prime(target):
    with open("primes.dat", "r") as fd:
        primes = fd.read().split("\n")
    candiK = iter(range(100, 0, -1))
    kObj = { 'k': next(candiK), 'candiK': candiK }
    hits = [*itertools.chain(*(optimizeK(kObj, int(prime), target) for prime in primes[100:-1]))]
    return hits


def optimizeK(kObj, prime, target):
    (k, candiK) = (kObj[i] for i in ('k', 'candiK'))
    current = pow(prime, k)
    current2 = current * 2
    print([k, prime, target, current, current2])
    while current > target:
        print([k, target, current, current2])
        k = next(candiK)
        current = current / prime
        current2 = current2 / prime
    k2 = k
    while current2 > target:
        print([k2, target, current2])
        k2 = k2 - 1
        current2 = current2 / prime
    kObj['k'] = k
    print([k, target-current, k2, target-current2])
    return [[prime, k, 1, target-current], [prime, k2, 2, target-current2]]


def mul_mod(a, b, m):
   if (not ((a | b) & (0xFFFFFFFF << 32))):
       return a * b % m
   i = 0
   d = 0
   mp2 = m >> 1
   if (a >= m): a %= m
   if (b >= m): b %= m
   while i < 64:
       if (d > mp2):
           d = (d << 1) - m
       else:
           d = d << 1
       if (a & 0x8000000000000000):
           d += b
       if (d >= m):
           d -= m
       a <<= 1
       i = i + 1
   return d


def order_pairs(p, g):
    p2 = p * p
    pm1 = p - 1
    yield([0, 0])
    i = g
    while i != 1:
        # i_norm = i - (math.floor(i / p))
        x = math.floor(i / p)
        y = i - (x * p)
        if y <= x:
            y = y - 1
        yield([x, y])
        i = (i * g) % p2

    
