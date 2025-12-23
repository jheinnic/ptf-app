from scipy.integrate import quad
import numpy as np
import pandas as pd

# x = np.linspace(-4, 4, num = 100)
# constant = 1.0 / np.sqrt(2*np.pi)
# pdf_normal_distribution = constant * np.exp((-x**2) / 2.0)
# fig, ax = plt.subplots(figsize=(10, 5));

def normalProbabilityDensity(x):
    constant = 1.0 / np.sqrt(2 * np.pi)
    return constant * np.exp((-x**2) / 2.0)

# 1. Define row and column headers
# We want rows to be 0.0, 0.1, 0.2 ... 3.4
# We want columns to be 0.00, 0.01 ... 0.09
indices = np.round(np.arange(-3.5, 3.5, 0.1), 2)
columns = np.round(np.arange(0.00, 0.1, 0.01), 2)

# 2. Initialize the data array (35 rows, 10 columns)
data = np.zeros((len(indices), len(columns)))

# 3. Fill the table using numerical integration
for i, row_val in enumerate(indices):
    for j, col_val in enumerate(columns):
        z = row_val + col_val
        # quad returns (integral_value, estimated_error)
        value, _ = quad(normalProbabilityDensity, -np.inf, z)
        data[i, j] = value

# 4. Display as a nice DataFrame for readability
z_table = pd.DataFrame(data, index=indices, columns=columns)

# Set options to show all rows and columns
pd.set_option('display.max_rows', None)
pd.set_option('display.max_columns', None)
pd.set_option('display.width', 1000) # Prevents line-wrapping
print(z_table)
