import numpy as np
from sklearn.cluster import KMeans
from sklearn.preprocessing import MinMaxScaler

kmeans = KMeans(
    n_clusters=2
)  # You want cluster the passenger records into 2: Survived or Not survived
kmeans.fit(X)
KMeans(
    algorithm="auto",
    copy_x=True,
    init="k-means++",
    max_iter=300,
    n_clusters=2,
    n_init=10,
    n_jobs=1,
    precompute_distances="auto",
    random_state=None,
    tol=0.0001,
    verbose=0,
)

kmeans = KMeans(n_clusters=2, max_iter=600, algorithm="auto")
kmeans.fit(X)
KMeans(
    algorithm="auto",
    copy_x=True,
    init="k-means++",
    max_iter=600,
    n_clusters=2,
    n_init=10,
    n_jobs=1,
    precompute_distances="auto",
    random_state=None,
    tol=0.0001,
    verbose=0,
)

correct = 0
for i in range(len(X)):
    predict_me = np.array(X[i].astype(float))
    predict_me = predict_me.reshape(-1, len(predict_me))
    prediction = kmeans.predict(predict_me)
    if prediction[0] == y[i]:
        correct += 1
print(correct / len(X))

scaler = MinMaxScaler()
X_scaled = scaler.fit_transform(X)
kmeans.fit(X_scaled)
KMeans(
    algorithm="auto",
    copy_x=True,
    init="k-means++",
    max_iter=600,
    n_clusters=2,
    n_init=10,
    n_jobs=1,
    precompute_distances="auto",
    random_state=None,
    tol=0.0001,
    verbose=0,
)
