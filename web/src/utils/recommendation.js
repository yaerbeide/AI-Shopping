/**
 * 协同过滤推荐系统工具函数
 * 实现基于用户的协同过滤算法，为用户推荐可能感兴趣的商品
 */

// 模拟用户行为数据（在实际应用中，这些数据应该从后端API获取）
const userBehaviorData = {
  // 用户ID: { 商品ID: 评分/兴趣度 }
  user1: { product1: 5, product2: 3, product3: 4, product6: 2 },
  user2: { product1: 4, product2: 5, product4: 3 },
  user3: { product2: 4, product3: 5, product5: 3, product6: 4 },
  user4: { product1: 5, product3: 3, product5: 5, product6: 2 },
  user5: { product2: 4, product4: 5, product5: 4 },
};

// 用户购买历史数据
const userPurchaseHistory = {
  user1: ['product1', 'product3'],
  user2: ['product2', 'product4'],
  user3: ['product3', 'product5'],
  user4: ['product1', 'product5'],
  user5: ['product2', 'product4'],
};

// 用户浏览历史数据
const userBrowsingHistory = {
  user1: ['product1', 'product2', 'product3', 'product6'],
  user2: ['product1', 'product2', 'product4', 'product5'],
  user3: ['product2', 'product3', 'product5', 'product6'],
  user4: ['product1', 'product3', 'product5', 'product6'],
  user5: ['product2', 'product4', 'product5', 'product1'],
};

/**
 * 计算两个用户之间的相似度（使用皮尔逊相关系数）
 * @param {Object} user1Ratings - 用户1的评分数据
 * @param {Object} user2Ratings - 用户2的评分数据
 * @returns {Number} - 相似度得分 (-1到1之间，1表示完全相似)
 */
function calculateSimilarity(user1Ratings, user2Ratings) {
  const commonProducts = [];

  // 找出两个用户共同评价过的商品
  for (const productId in user1Ratings) {
    if (user2Ratings[productId]) {
      commonProducts.push(productId);
    }
  }

  // 如果没有共同评价的商品，返回0
  if (commonProducts.length === 0) return 0;

  // 计算评分和
  let sum1 = 0;
  let sum2 = 0;
  let sum1Sq = 0;
  let sum2Sq = 0;
  let pSum = 0;

  for (const productId of commonProducts) {
    const rating1 = user1Ratings[productId];
    const rating2 = user2Ratings[productId];

    sum1 += rating1;
    sum2 += rating2;
    sum1Sq += Math.pow(rating1, 2);
    sum2Sq += Math.pow(rating2, 2);
    pSum += rating1 * rating2;
  }

  // 计算皮尔逊相关系数
  const num = pSum - (sum1 * sum2) / commonProducts.length;
  const den = Math.sqrt((sum1Sq - Math.pow(sum1, 2) / commonProducts.length) * (sum2Sq - Math.pow(sum2, 2) / commonProducts.length));

  if (den === 0) return 0;

  return num / den;
}

/**
 * 找出与当前用户最相似的用户
 * @param {String} userId - 当前用户ID
 * @returns {Array} - 按相似度排序的用户数组
 */
function findSimilarUsers(userId) {
  const similarities = [];
  const currentUserRatings = userBehaviorData[userId] || {};

  // 计算当前用户与其他用户的相似度
  for (const otherUserId in userBehaviorData) {
    if (otherUserId !== userId) {
      const similarity = calculateSimilarity(currentUserRatings, userBehaviorData[otherUserId]);

      similarities.push({
        userId: otherUserId,
        similarity: similarity,
      });
    }
  }

  // 按相似度降序排序
  return similarities.sort((a, b) => b.similarity - a.similarity);
}

/**
 * 基于用户相似度推荐商品
 * @param {String} userId - 用户ID
 * @param {Array} allProducts - 所有可用商品列表
 * @param {Number} numRecommendations - 推荐商品数量
 * @returns {Array} - 推荐商品列表
 */
export function getRecommendedProducts(userId, allProducts, numRecommendations = 4) {
  // 如果用户ID不存在或为空，返回随机商品
  if (!userId) {
    return [...allProducts].sort(() => Math.random() - 0.5).slice(0, numRecommendations);
  }

  // 获取相似用户
  const similarUsers = findSimilarUsers(userId);

  // 如果没有相似用户，返回随机商品
  if (similarUsers.length === 0) {
    return [...allProducts].sort(() => Math.random() - 0.5).slice(0, numRecommendations);
  }

  // 获取当前用户已经购买或浏览过的商品ID
  const userHistory = [...(userPurchaseHistory[userId] || []), ...(userBrowsingHistory[userId] || [])];

  // 创建推荐得分映射
  const recommendationScores = {};

  // 基于相似用户的评分计算推荐得分
  for (const { userId: similarUserId, similarity } of similarUsers) {
    // 只考虑相似度为正的用户
    if (similarity <= 0) continue;

    const userRatings = userBehaviorData[similarUserId] || {};

    for (const productId in userRatings) {
      // 跳过用户已经购买或浏览过的商品
      if (userHistory.includes(productId)) continue;

      // 计算加权得分
      const score = similarity * userRatings[productId];

      if (!recommendationScores[productId]) {
        recommendationScores[productId] = 0;
      }

      recommendationScores[productId] += score;
    }
  }

  // 将推荐得分转换为数组并排序
  const recommendations = Object.entries(recommendationScores)
    .map(([productId, score]) => ({ productId, score }))
    .sort((a, b) => b.score - a.score);

  // 如果推荐列表为空，返回随机商品
  if (recommendations.length === 0) {
    return [...allProducts].sort(() => Math.random() - 0.5).slice(0, numRecommendations);
  }

  // 将推荐的商品ID映射到实际商品对象
  const recommendedProducts = recommendations
    .map((rec) => {
      // 在实际应用中，这里应该是通过productId查找商品信息
      // 这里我们模拟一个查找过程
      const product = allProducts.find((p) => p.id === rec.productId);
      return product;
    })
    .filter(Boolean); // 过滤掉未找到的商品

  // 如果推荐商品不足，用随机商品补充
  if (recommendedProducts.length < numRecommendations) {
    const remainingProducts = allProducts.filter(
      (product) => !recommendedProducts.some((rec) => rec.id === product.id) && !userHistory.includes(product.id),
    );

    const additionalProducts = [...remainingProducts]
      .sort(() => Math.random() - 0.5)
      .slice(0, numRecommendations - recommendedProducts.length);

    return [...recommendedProducts, ...additionalProducts];
  }

  return recommendedProducts.slice(0, numRecommendations);
}

/**
 * 更新用户行为数据（在实际应用中，这应该是一个API调用）
 * @param {String} userId - 用户ID
 * @param {String} productId - 商品ID
 * @param {Number} rating - 评分/兴趣度
 */
export function updateUserBehavior(userId, productId, rating) {
  if (!userBehaviorData[userId]) {
    userBehaviorData[userId] = {};
  }

  userBehaviorData[userId][productId] = rating;
}

/**
 * 记录用户购买行为
 * @param {String} userId - 用户ID
 * @param {String} productId - 商品ID
 */
export function recordPurchase(userId, productId) {
  if (!userPurchaseHistory[userId]) {
    userPurchaseHistory[userId] = [];
  }

  if (!userPurchaseHistory[userId].includes(productId)) {
    userPurchaseHistory[userId].push(productId);
  }

  // 购买行为通常表示较高的兴趣度
  updateUserBehavior(userId, productId, 5);
}

/**
 * 记录用户浏览行为
 * @param {String} userId - 用户ID
 * @param {String} productId - 商品ID
 */
export function recordBrowsing(userId, productId) {
  if (!userBrowsingHistory[userId]) {
    userBrowsingHistory[userId] = [];
  }

  if (!userBrowsingHistory[userId].includes(productId)) {
    userBrowsingHistory[userId].push(productId);
  }

  // 浏览行为通常表示中等的兴趣度
  updateUserBehavior(userId, productId, 3);
}

/**
 * 为新用户生成初始推荐
 * @param {Array} allProducts - 所有可用商品列表
 * @param {Number} numRecommendations - 推荐商品数量
 * @returns {Array} - 推荐商品列表
 */
export function getInitialRecommendations(allProducts, numRecommendations = 4) {
  // 对于新用户，我们可以推荐热门商品或随机商品
  // 这里简单地返回随机商品
  return [...allProducts].sort(() => Math.random() - 0.5).slice(0, numRecommendations);
}
