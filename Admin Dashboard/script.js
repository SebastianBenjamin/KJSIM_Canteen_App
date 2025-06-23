// Import Firebase functions
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";
import { getDatabase, ref, onValue, update, remove, set } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-database.js";
import { getAnalytics } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-analytics.js";

// Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyCR6tZIByJSmI47OBXAns-JsrxzqJbhPgY",
  authDomain: "canteenapp-32d6a.firebaseapp.com",
  databaseURL: "https://canteenapp-32d6a-default-rtdb.firebaseio.com",
  projectId: "canteenapp-32d6a",
  storageBucket: "canteenapp-32d6a.firebasestorage.app",
  messagingSenderId: "911365416862",
  appId: "1:911365416862:web:44f25747b25be5cac9188f",
  measurementId: "G-JX2PW2W4G3"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const database = getDatabase(app);

// Wait for DOM to load
document.addEventListener('DOMContentLoaded', () => {
  // DOM elements
  const mobileToggle = document.getElementById('mobileToggle');
  const sidebar = document.getElementById('sidebar');
  const menuItems = document.querySelectorAll('.menu-item');
  const contentSections = document.querySelectorAll('.content-section');
  const pageTitle = document.getElementById('pageTitle');
  
  // Orders section
  const ordersSection = document.getElementById('ordersSection');
  const ordersList = document.getElementById('ordersList');
  const orderFilter = document.getElementById('orderFilter');
  
  // Items section
  const itemsSection = document.getElementById('itemsSection');
  const itemsList = document.getElementById('itemsList');
  const addItemBtn = document.getElementById('addItemBtn');
  
  // Customers section
  const customersSection = document.getElementById('customersSection');
  const customersList = document.getElementById('customersList');
  const customerSearch = document.getElementById('customerSearch');

  // Carts section
  const cartsSection = document.getElementById('cartsSection');
  const cartsList = document.getElementById('cartsList');
  
  // Item modal
  const itemModal = document.getElementById('itemModal');
  const modalTitle = document.getElementById('modalTitle');
  const itemForm = document.getElementById('itemForm');
  const itemCodeInput = document.getElementById('itemCode');
  const itemNameInput = document.getElementById('itemName');
  const itemPriceInput = document.getElementById('itemPrice');
  const itemCategoryInput = document.getElementById('itemCategory');
  const closeModalBtn = document.getElementById('closeModalBtn');
  const cancelModalBtn = document.getElementById('cancelModalBtn');
  const saveItemBtn = document.getElementById('saveItemBtn');

  // Current active tab
  let currentTab = 'orders';

  // Initialize the app
  function init() {
    setupEventListeners();
    showSection(currentTab);
    loadData();
  }

  // Set up event listeners
  function setupEventListeners() {
    // Mobile sidebar toggle
    mobileToggle.addEventListener('click', () => {
      sidebar.classList.toggle('-translate-x-full');
    });

    // Menu item clicks
    menuItems.forEach(item => {
      item.addEventListener('click', () => {
        const tab = item.getAttribute('data-tab');
        if (tab) {
          currentTab = tab;
          showSection(tab);
        }
      });
    });

    // Order filter change
    if (orderFilter) {
      orderFilter.addEventListener('change', () => {
        loadOrders();
      });
    }

    // Add item button
    if (addItemBtn) {
      addItemBtn.addEventListener('click', () => {
        openItemModal();
      });
    }

    // Customer search
    if (customerSearch) {
      customerSearch.addEventListener('input', () => {
        loadCustomers();
      });
    }

    // Modal buttons
    if (closeModalBtn) closeModalBtn.addEventListener('click', closeItemModal);
    if (cancelModalBtn) cancelModalBtn.addEventListener('click', closeItemModal);
    if (saveItemBtn) saveItemBtn.addEventListener('click', saveItem);
  }

  // Show the appropriate section based on tab
  function showSection(tab) {
    // Update active menu item
    menuItems.forEach(item => {
      if (item.getAttribute('data-tab') === tab) {
        item.classList.add('active-tab');
      } else {
        item.classList.remove('active-tab');
      }
    });

    // Update page title
    switch(tab) {
      case 'orders':
        pageTitle.textContent = 'Orders';
        break;
      case 'items':
        pageTitle.textContent = 'Manage Items';
        break;
      case 'customers':
        pageTitle.textContent = 'Customers';
        break;
      case 'carts':
        pageTitle.textContent = 'Active Carts';
        break;
    }

    // Show the correct section
    contentSections.forEach(section => {
      if (section.id === `${tab}Section`) {
        section.classList.remove('hidden');
      } else {
        section.classList.add('hidden');
      }
    });
  }

  // Load all data
  function loadData() {
    loadOrders();
    loadItems();
    loadCustomers();
    loadCarts();
  }

  // Load orders from Firebase
  function loadOrders() {
    const ordersRef = ref(database, 'Orders');
    const itemsRef = ref(database, 'Items');
    
    onValue(ordersRef, (ordersSnapshot) => {
      const ordersData = ordersSnapshot.val();
      const filterValue = orderFilter ? orderFilter.value : 'all';
      
      if (!ordersList) return;
      ordersList.innerHTML = '';
      
      if (!ordersData) {
        ordersList.innerHTML = '<tr><td colspan="7" class="p-4 text-center">No orders found</td></tr>';
        return;
      }
      
      let hasOrders = false;
      
      for (const orderId in ordersData) {
        const order = ordersData[orderId];
        
        // Apply filter
        if (filterValue !== 'all' && order.status !== filterValue) {
          continue;
        }
        
        hasOrders = true;
        
        // Format items list with names
        let itemsHtml = '';
        let total = 0;
        
        if (order.items && Array.isArray(order.items)) {
          order.items.forEach(item => {
            if (item && item.itemName) {
              itemsHtml += `<div>${item.itemName} (x${item.itemQuantity}) - ₹${item.itemPrice * item.itemQuantity}</div>`;
              total += item.itemPrice * item.itemQuantity;
            }
          });
        }
        
        // Format timestamp
        const orderDate = order.orderDate || 'Not specified';
        
        // Status badge
        let statusClass = '';
        switch(order.status) {
          case 'pending':
            statusClass = 'bg-yellow-100 text-yellow-800';
            break;
          case 'completed':
            statusClass = 'bg-green-100 text-green-800';
            break;
          case 'cancelled':
            statusClass = 'bg-red-100 text-red-800';
            break;
          default:
            statusClass = 'bg-gray-100 text-gray-800';
        }
        
        // Create row
        const row = document.createElement('tr');
        row.className = 'border-b hover:bg-gray-50';
        row.innerHTML = `
          <td class="p-3">${orderId}</td>
          <td class="p-3">${order.customerId || 'N/A'}</td>
          <td class="p-3">${itemsHtml}</td>
          <td class="p-3 font-semibold">₹${total.toFixed(2)}</td>
          <td class="p-3">
            <span class="px-2 py-1 rounded-full text-xs ${statusClass}">${order.status || 'pending'}</span>
          </td>
          <td class="p-3">${orderDate}</td>
          <td class="p-3">
            <div class="flex gap-2">
              ${order.status === 'pending' ? `
              <button class="px-2 py-1 bg-green-500 text-white text-xs rounded complete-order" data-orderid="${orderId}">
                Complete
              </button>
              <button class="px-2 py-1 bg-red-500 text-white text-xs rounded cancel-order" data-orderid="${orderId}">
                Cancel
              </button>
              ` : ''}
            </div>
          </td>
        `;
        
        ordersList.appendChild(row);
      }
      
      if (!hasOrders) {
        ordersList.innerHTML = '<tr><td colspan="7" class="p-4 text-center">No orders match the selected filter</td></tr>';
      }
      
      // Add event listeners to order action buttons
      document.querySelectorAll('.complete-order').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const orderId = e.target.getAttribute('data-orderid');
          updateOrderStatus(orderId, 'completed');
        });
      });
      
      document.querySelectorAll('.cancel-order').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const orderId = e.target.getAttribute('data-orderid');
          updateOrderStatus(orderId, 'cancelled');
        });
      });
    });
  }

  // Update order status
  function updateOrderStatus(orderId, status) {
    const orderRef = ref(database, `Orders/${orderId}`);
    update(orderRef, { status: status })
      .then(() => {
        console.log('Order status updated successfully');
      })
      .catch((error) => {
        console.error('Error updating order status:', error);
      });
  }

  // Load items from Firebase
  function loadItems() {
    const itemsRef = ref(database, 'Items');
    onValue(itemsRef, (snapshot) => {
      const itemsData = snapshot.val();
      
      if (!itemsList) return;
      itemsList.innerHTML = '';
      
      if (!itemsData) {
        itemsList.innerHTML = '<tr><td colspan="6" class="p-4 text-center">No items found</td></tr>';
        return;
      }
      
      for (const itemCode in itemsData) {
        const item = itemsData[itemCode];
        
        const row = document.createElement('tr');
        row.className = 'border-b hover:bg-gray-50';
        row.innerHTML = `
          <td class="p-3">${itemCode}</td>
          <td class="p-3">${item.name || 'N/A'}</td>
          <td class="p-3 font-semibold">₹${item.price ? item.price.toFixed(2) : '0.00'}</td>
          <td class="p-3">
            <span class="px-2 py-1 rounded-full text-xs bg-gray-100">${item.categoryId || 'N/A'}</span>
          </td>
          <td class="p-3">
            <div class="flex gap-2">
              <button class="px-2 py-1 bg-blue-500 text-white text-xs rounded edit-item" data-itemcode="${itemCode}">
                <i class="fas fa-edit mr-1"></i> Edit
              </button>
              <button class="px-2 py-1 bg-red-500 text-white text-xs rounded delete-item" data-itemcode="${itemCode}">
                <i class="fas fa-trash mr-1"></i> Delete
              </button>
            </div>
          </td>
        `;
        
        itemsList.appendChild(row);
      }
      
      // Add event listeners to item action buttons
      document.querySelectorAll('.edit-item').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const itemCode = e.target.getAttribute('data-itemcode');
          const item = itemsData[itemCode];
          openItemModal(itemCode, item);
        });
      });
      
      document.querySelectorAll('.delete-item').forEach(btn => {
        btn.addEventListener('click', (e) => {
          const itemCode = e.target.getAttribute('data-itemcode');
          if (confirm('Are you sure you want to delete this item?')) {
            deleteItem(itemCode);
          }
        });
      });
    });
  }

  // Load customers from Firebase
  function loadCustomers() {
    const customersRef = ref(database, 'Customers');
    onValue(customersRef, (snapshot) => {
      const customersData = snapshot.val();
      const searchTerm = customerSearch ? customerSearch.value.toLowerCase() : '';
      
      if (!customersList) return;
      customersList.innerHTML = '';
      
      if (!customersData) {
        customersList.innerHTML = '<tr><td colspan="7" class="p-4 text-center">No customers found</td></tr>';
        return;
      }
      
      let hasCustomers = false;
      
      for (const customerId in customersData) {
        const customer = customersData[customerId];
        
        // Apply search filter
        if (searchTerm && 
            !(customer.name && customer.name.toLowerCase().includes(searchTerm)) && 
            !(customer.email && customer.email.toLowerCase().includes(searchTerm))) {
          continue;
        }
        
        hasCustomers = true;
        
        const row = document.createElement('tr');
        row.className = 'border-b hover:bg-gray-50';
        row.innerHTML = `
          <td class="p-3">${customerId}</td>
          <td class="p-3">${customer.name || 'N/A'}</td>
          <td class="p-3">${customer.email || 'N/A'}</td>
          <td class="p-3">${customer.phone || 'N/A'}</td>
          <td class="p-3">${customer.orderCount || 0}</td>
          <td class="p-3">${customer.lastOrder ? new Date(customer.lastOrder).toLocaleString() : 'N/A'}</td>
          <td class="p-3">${customer.currentCart ? customer.currentCart : 'No active cart'}</td>
        `;
        
        customersList.appendChild(row);
      }
      
      if (!hasCustomers) {
        customersList.innerHTML = '<tr><td colspan="7" class="p-4 text-center">No customers match the search</td></tr>';
      }
    });
  }

  // Load carts from Firebase
  function loadCarts() {
    const cartsRef = ref(database, 'Carts');
    const itemsRef = ref(database, 'Items');
    
    onValue(itemsRef, (itemsSnapshot) => {
      const itemsData = itemsSnapshot.val();
      
      onValue(cartsRef, (cartsSnapshot) => {
        const cartsData = cartsSnapshot.val();
        
        if (!cartsList) return;
        cartsList.innerHTML = '';
        
        if (!cartsData) {
          cartsList.innerHTML = '<tr><td colspan="7" class="p-4 text-center">No active carts found</td></tr>';
          return;
        }
        
        for (const cartId in cartsData) {
          const cart = cartsData[cartId];
          
          // Format items list with names
          let itemsHtml = '';
          let subtotal = 0;
          
          if (cart.items) {
            for (const itemId in cart.items) {
              const item = cart.items[itemId];
              const itemCode = item.itemCode;
              const itemName = itemsData[itemCode]?.name || itemCode;
              itemsHtml += `<div>${itemName} (x${item.quantity})</div>`;
              subtotal += (item.price || 0) * (item.quantity || 1);
            }
          }
          
          const row = document.createElement('tr');
          row.className = 'border-b hover:bg-gray-50';
          row.innerHTML = `
            <td class="p-3">${cartId}</td>
            <td class="p-3">${cart.customerId || 'N/A'}</td>
            <td class="p-3">${itemsHtml}</td>
            <td class="p-3 font-semibold">₹${subtotal.toFixed(2)}</td>
            <td class="p-3">${cart.createdAt ? new Date(cart.createdAt).toLocaleString() : 'N/A'}</td>
            <td class="p-3">
              <div class="flex gap-2">
                <button class="px-2 py-1 bg-green-500 text-white text-xs rounded convert-to-order" data-cartid="${cartId}">
                  Convert to Order
                </button>
                <button class="px-2 py-1 bg-red-500 text-white text-xs rounded delete-cart" data-cartid="${cartId}">
                  Delete
                </button>
              </div>
            </td>
          `;
          
          cartsList.appendChild(row);
        }
        
        // Add event listeners to cart action buttons
        document.querySelectorAll('.convert-to-order').forEach(btn => {
          btn.addEventListener('click', (e) => {
            const cartId = e.target.getAttribute('data-cartid');
            convertCartToOrder(cartId);
          });
        });
        
        document.querySelectorAll('.delete-cart').forEach(btn => {
          btn.addEventListener('click', (e) => {
            const cartId = e.target.getAttribute('data-cartid');
            if (confirm('Are you sure you want to delete this cart?')) {
              deleteCart(cartId);
            }
          });
        });
      });
    });
  }

  // Convert cart to order
  function convertCartToOrder(cartId) {
    // Get cart data
    const cartRef = ref(database, `Carts/${cartId}`);
    onValue(cartRef, (snapshot) => {
      const cartData = snapshot.val();
      if (!cartData) {
        alert('Cart not found!');
        return;
      }
      
      // Create new order
      const newOrderId = Date.now().toString(); // Using timestamp as order ID
      const orderData = {
        customerId: cartData.customerId,
        items: cartData.items,
        totalAmount: cartData.subtotal,
        totalItems: Object.keys(cartData.items || {}).length,
        status: 'pending',
        orderDate: new Date().toLocaleString('en-IN', {
          day: '2-digit',
          month: '2-digit',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        }).replace(/\//g, '-')
      };
      
      // Add order to Firebase
      const orderRef = ref(database, `Orders/${newOrderId}`);
      set(orderRef, orderData)
        .then(() => {
          console.log('Order created successfully');
          
          // Update customer's order count and last order
          const customerRef = ref(database, `Customers/${cartData.customerId}`);
          onValue(customerRef, (customerSnapshot) => {
            const customerData = customerSnapshot.val();
            if (customerData) {
              const updatedOrderCount = (customerData.orderCount || 0) + 1;
              update(customerRef, {
                orderCount: updatedOrderCount,
                lastOrder: new Date().toISOString(),
                currentCart: null
              });
            }
          }, { once: true });
          
          // Delete the cart
          deleteCart(cartId);
        })
        .catch((error) => {
          console.error('Error creating order:', error);
        });
    }, { once: true });
  }

  // Delete cart
  function deleteCart(cartId) {
    const cartRef = ref(database, `Carts/${cartId}`);
    remove(cartRef)
      .then(() => {
        console.log('Cart deleted successfully');
      })
      .catch((error) => {
        console.error('Error deleting cart:', error);
      });
  }

  // Open item modal for add/edit
  function openItemModal(itemCode = null, itemData = null) {
    if (!itemModal) return;
    
    if (itemCode && itemData) {
      // Edit mode
      modalTitle.textContent = 'Edit Item';
      itemCodeInput.value = itemCode;
      itemNameInput.value = itemData.name || '';
      itemPriceInput.value = itemData.price || 0;
      itemCategoryInput.value = itemData.categoryId || '';
      
      // Disable item code in edit mode
      itemCodeInput.disabled = true;
    } else {
      // Add mode
      modalTitle.textContent = 'Add New Item';
      itemForm.reset();
      
      // Enable item code in add mode
      itemCodeInput.disabled = false;
    }
    
    itemModal.classList.remove('hidden');
  }

  // Close item modal
  function closeItemModal() {
    if (!itemModal) return;
    itemModal.classList.add('hidden');
  }

  // Save item (add or update)
  function saveItem() {
    const itemCode = itemCodeInput.value;
    if (!itemCode) {
      alert('Item code is required');
      return;
    }
    
    const itemData = {
      name: itemNameInput.value,
      price: parseFloat(itemPriceInput.value),
      categoryId: itemCategoryInput.value
    };
    
    const itemRef = ref(database, `Items/${itemCode}`);
    set(itemRef, itemData)
      .then(() => {
        console.log('Item saved successfully');
        closeItemModal();
      })
      .catch((error) => {
        console.error('Error saving item:', error);
      });
  }

  // Delete item
  function deleteItem(itemCode) {
    const itemRef = ref(database, `Items/${itemCode}`);
    remove(itemRef)
      .then(() => {
        console.log('Item deleted successfully');
      })
      .catch((error) => {
        console.error('Error deleting item:', error);
      });
  }

  // Initialize the app
  init();
});