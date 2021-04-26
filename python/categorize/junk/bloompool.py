# Python 3 program to build Bloom Filter 
# Install mmh3 and bitarray 3rd party module first 
# pip install mmh3 
# pip install bitarray 
import math 
import mmh3 
from bitarray import bitarray 
from struct import pack
from typing import Optional, Iterator, List
  
class BloomFilter(object): 
  
    """
    Class for Bloom filter, using murmur3 hash function 
    """
  
    def __init__(self, items_count, fp_prob, strictly_unique: bool=False, seed_gen: Optional[Iterator[int]] = None): 
        """
        items_count : int 
            Number of items expected to be stored in bloom filter 
        fp_prob : float 
            False Positive probability in decimal 
        """
        # False posible probability in decimal 
        self.fp_prob = fp_prob 
  
        # Size of bit array to use 
        self.size = self.get_size(items_count,fp_prob) 
  
        # number of hash functions to use 
        self.hash_count = self.get_hash_count(self.size,items_count) 

        # iterable for enumerating hash functions
        self.hash_seeds = range(self.hash_count) if (seed_gen is None) else [next(seed_gen) for x in range(self.hash_count)]
  
        # Bit array of given size 
        self.bit_array = bitarray(self.size) 
  
        # initialize all bits as 0 
        self.bit_array.setall(0) 

        # Plan for converting the array of digest indices to a unique address in hexadecimal iff
        # strict uniqueness is to be enforced.
        if strictly_unique:
            # Bit packing plan for encoding an array of hash indices
            self._split_plans = [plan for plan in self._generate_split_plans()]

            # Number of bytes needed to encode the output of however many hash functions are in use
            self.address_length = math.ceil(self.hash_count * self.size.bit_length() / 8)

            # Format string for serializing a bit-packed hash index address
            self._pack_format = '<' + ('B' * self.address_length)
        else:
            self._split_plans = []
            self.address_length = 0
            self._pack_format = ''

        # If enforcing strict uniqueness, only add new filter elements if there is no possibility
        # of a conflict.  This has a false positive chance of rejecting genuinely unique entries,
        # but also allows the bloom filter to be used to generate unique identifiers for each 
        # member it accepts without extra book keeping.
        self._strictly_unique = strictly_unique

    def add(self, item): 
        """
        Add an item in the filter 
        """
        unique = False
        if self._strictly_unique:
            hash_indices = [hash_index for hash_index in self._iterate_hashes(item)]
            unique = self._add(hash_indices)
            if unique:
                return self._get_bloom_address(hash_indices)
        else:
            unique = self._add(self._iterate_hashes(item))

        return unique

    def check(self, item): 
        """
        Check for possible existence of an item in filter without adding it.
        A false value is certain, a true value may be a false positive.
        """
        unique: bool = False
        if self._strictly_unique:
            hash_indices = [hash_index for hash_index in self._iterate_hashes(item)]
            unique = self._check(hash_indices)
            if unique:
                return self._get_bloom_address(hash_indices)
        else:
            unique = self._check(self._iterate_hashes(item))

        return unique

    def _add(self, hash_iter):
        unique = False
        for hash_index in hash_iter:
            if not self.bit_array[hash_index]:
                unique = True
            self.bit_array[hash_index] = True
        return unique

    def _check(self, hash_iter):
        for hash_index in hash_iter:
            if not self.bit_array[hash_index]:
                return False
        return True
  
    def _iterate_hashes(self, item):
        """
        create digest for given item. 
        With different seeds, digest created is different 
        """
        for seed in self.hash_seeds:
            yield mmh3.hash(item, seed) % self.size

    @classmethod
    def get_size(self,n,p): 
        """
        Return the size of bit array(m) to used using 
        following formula 
        m = -(n * lg(p)) / (lg(2)^2) 
        n : int 
            number of items expected to be stored in filter 
        p : float 
            False Positive probability in decimal 
        """
        m = -(n * math.log(p))/(math.log(2)**2) 
        return int(m) 
  
    @classmethod
    def get_hash_count(self, m, n): 
        """
        Return the hash function(k) to be used using 
        following formula 
        k = (m/n) * lg(2) 
  
        m : int 
            size of bit array 
        n : int 
            number of items expected to be stored in filter 
        """
        k = (m/n) * math.log(2) 
        return int(k) 

    def _get_bloom_address(self, indices:List[int]):
        hash_indices = [index for index in self._split_hash_indices(indices)]
        return pack(self._pack_format, *hash_indices).hex()

    def _split_hash_indices(self, indices:List[int]):
        carry: int = 0
        partial: int = 0
        value: int = 0
        split_plan: List[int] = []
        for (value, split_plan) in zip(indices, self._split_plans):
            for bits in split_plan:
                remnant = value >> bits
                if carry > 0:
                    partial = (partial << bits) + value - (remnant << bits)
                    carry = carry + bits
                else:
                    partial = value - (remnant << bits)
                    carry = bits

                if carry == 8:
                    yield partial
                    value = remnant
                    partial = 0
                    carry = 0
        if carry > 0:
            yield partial << (8 - carry)

    @classmethod
    def _plan_next_split(cls, carry, bits):
        if carry >= bits:
            yield bits
            return
        if carry > 0:
            yield carry
            bits = bits - carry
        while bits > 8:
            yield 8
            bits = bits - 8
        if bits > 0:
            yield bits

    def _generate_split_plans(self):
        carry = 0
        bits_per_index = self.size.bit_length()
        
        for x in range(0, self.hash_count):
            split_plan = [chunk_size for chunk_size in self._plan_next_split(carry, bits_per_index)]
            if len(split_plan) > 1:
                carry = 8 - split_plan[-1]
            elif carry > 0:
                carry = carry - split_plan[0]
            else:
                carry = 8 - split_plan[0]
            yield split_plan

