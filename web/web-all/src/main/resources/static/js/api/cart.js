var cart = {

  api_name: '/api/cart',

  // 添加购物车
  addToCart(skuId, skuNum) {
    return request({
      url: this.api_name + '/addToCart/' + skuId + '/' + skuNum,
      method: 'post'
    })
  },

  // 我的购物车
  cartList() {
    return request({
      url: this.api_name + '/cartList', //"http://localhost:8201/api/cart"
      method: 'get'
    })
  },

  // 更新选中状态
  checkCart(skuId, isChecked) {
    return request({
      url: this.api_name + '/checkCart/' + skuId + '/' + isChecked,//"http://localhost:8201/api/cart"
      method: 'get'
    })
  },

// 刪除
  deleteCart(skuId) {
    return request({
      url: this.api_name + '/deleteCart/' + skuId,
      method: 'delete'
    })
  }
}
