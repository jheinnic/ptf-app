class Foo:
    def __init__(self):
        self.b = 3

    @classmethod
    def bar(cls):
        print("Bar\n")

    FOO = {
        "a": bar
    }

    def do(self):
        Foo.FOO["a"].call()

#b = Foo()
#b.do()

