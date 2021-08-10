# Import all libraries for this portion of the blog post
from scipy.integrate import quad
import numpy as np

x = np.linspace(-4, 4, num = 100)
constant = 1.0 / np.sqrt(2*np.pi)
pdf_normal_distribution = constant * np.exp((-x**2) / 2.0)
fig, ax = plt.subplots(figsize=(10, 5));

def normalProbabilityDensity(x):
    constant = 1.0 / np.sqrt(2*np.pi)
    return(constant * np.exp((-x**2) / 2.0) )

indices = np.round(np.arange(0, 3.5, 1), 2),
columns = np.round(n.arange(0.00, 0.1, 0.01), 2)
data = np.ndarray([35, 10])

ii = 0
for index in indices: 
    jj = 0
    for column in columns:
        z = np.round(index + column, 2)
        value, _ = quad(normalProbabilityDensity, np.NINF, z)
        data[ii, jj] = value
        jj = jj + 1
    ii = ii + 1


