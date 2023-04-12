# code modified from https://medium.com/@jacob.d.moore1/coding-the-simplex-algorithm-from-scratch-using-python-and-numpy-93e3813e6e70

import numpy as np

def gen_matrix(vars, cons):
    # generate a matrix table with enough rows for each constraint and objective 
    # function, and enough columns for variables and slack variables
    turn_table = np.zeros((cons + 1, vars + cons + 2))
    return turn_table

def need_pivot_col(table):
    # check if the table needs a pivot by checking last column
    min_col = min(table[:-1, -1])
    return min_col < 0

def need_pivot_row(table):
    # check if the table needs a pivot by checking last row
    min_row = min(table[-1, :-1])
    return min_row < 0

def find_neg_col(table):
    # find negative values in rightmost column
    m = min(table[:-1, -1])
    return np.where(table[:-1, -1] == m)[0][0] if m <= 0 else None

def find_neg_row(table):
    # find negative values in bottom row
    lr = table.shape[0]
    m = min(table[-1, :-1])
    return np.where(table[-1, :-1] == m)[0][0] if m <= 0 else None

def loc_piv_col(table):
    #find pivot values in column
    total = []        
    r = find_neg_col(table)
    row = table[r, :-1]
    m = min(row)
    c = np.where(row == m)[0][0]
    col = table[:-1, c]
    for i, b in zip(col, table[:-1, -1]):
        if i**2 > 0 and b/i > 0:
            total.append(b/i)
        else:                
            total.append(10000)
    index = total.index(min(total))        
    return [index, c]

def loc_pivot_row(table):
    #find pivot values in row
    if need_pivot_col(table):
        total = []
        n = find_neg_row(table)
        for i, b in zip(table[:-1, n],table[:-1, -1]):
            if b/i > 0 and i**2 > 0:
                total.append(b/i)
            else:
                total.append(10000)
        index = total.index(min(total))
        return [index,n]
    
def pivot(row, col, table):
    #pivot the table
    t = np.zeros(table.shape)
    pr = table[row, :]
    if table[row, col]**2 > 0:
        e = 1 / table[row, col]
        r = pr * e
        for i in range(len(table[:, col])):
            k = table[i, :]
            c = table[i, col]
            if list(k) != list(pr):
                t[i,:] = list(k-r*c)
        t[row, :] = list(r)
        return t
    else:
        print('Cannot pivot on this element.')
        
def convert(eq):
    # convert user input string to float variables
    # G means >= and L means <=
    eq = eq.split(',')
    if 'G' in eq:
        g = eq.index('G')
        del eq[g]
        eq = [float(i) * -1 for i in eq]
        return eq
    if 'L' in eq:
        l = eq.index('L')
        del eq[l]
        eq = [float(i) for i in eq]
        return eq
    
def convert_min(table):
    # modify for minimisation problem
    table[-1, :-2] = [-1 * i for i in table[-1, :-2]]
    table[-1, -1] = -1 * table[-1, -1]    
    return table

def gen_var(table):
    # generate only the required number of variables x1..xn
    lc = table.shape[1]
    lr = table.shape[0]
    var = lc - lr -1
    v = []
    for i in range(var):
        v.append('x'+ str(i + 1))
    return v

def add_cons(table):
    # check if we can add constraints to the matrix
    lr = table.shape[0]
    empty = []
    for i in range(lr):
        total = 0
        for j in table[i, :]:                       
            total += j**2
        if total == 0: 
            empty.append(total)
    return len(empty) > 1

def constrain(table,eq):
    # add a constraint to the matrix
    if add_cons(table):
        lc = table.shape[1]
        lr = table.shape[0]
        var = lc - lr -1      
        j = 0
        while j < lr:            
            row_check = table[j, :]
            total = 0
            for i in row_check:
                total += float(i**2)
            if total == 0:                
                row = row_check
                break
            j += 1
        eq = convert(eq)
        i = 0
        while i < len(eq) - 1:
            row[i] = eq[i]
            i += 1        
        row[-1] = eq[-1]
        row[var + j] = 1    
    else:
        print('Cannot add another constraint.')
        
def add_obj(table):
    # check if objective function can be added to tableau
    lr = table.shape[0]
    empty = []
    for i in range(lr):
        total = 0        
        for j in table[i, :]:
            total += j**2
        if total == 0:
            empty.append(total)    
    return len(empty) == 1

def obj(table,eq):
    # add objective function to tableau once constraint functions have been added
    if add_obj(table):
        eq = [float(i) for i in eq.split(',')]
        lr = table.shape[0]
        row = table[lr - 1, :]
        i = 0        
        while i < len(eq)-1:
            row[i] = eq[i] * -1
            i += 1
        row[-2] = 1
        row[-1] = eq[-1]
    else:
        print('You must finish adding constraints before the objective function can be added.')
        
def maxz(table):
    # create maximization function
    while need_pivot_col(table):
        table = pivot(loc_piv_col(table)[0], loc_piv_col(table)[1],table)
    while need_pivot_row(table):
        table = pivot(loc_pivot_row(table)[0], loc_pivot_row(table)[1],table)        
    lc = table.shape[1]
    lr = table.shape[0]
    var = lc - lr -1
    i = 0
    val = {}
    for i in range(var):
        col = table[:, i]
        s = sum(col)
        m = max(col)
        if float(s) == float(m):
            loc = np.where(col == m)[0][0]            
            val[gen_var(table)[i]] = table[loc, -1]
        else:
            val[gen_var(table)[i]] = 0
    val['max'] = table[-1, -1]
    return val

def minz(table):
    # create minimization function
    table = convert_min(table)
    while need_pivot_col(table):
        table = pivot(loc_piv_col(table)[0],loc_piv_col(table)[1],table)    
    while need_pivot_row(table):
        table = pivot(loc_pivot_row(table)[0],loc_pivot_row(table)[1],table)       
    lc = table.shape[1]
    lr = table.shape[0]
    var = lc - lr - 1
    i = 0
    val = {}
    for i in range(var):
        col = table[:, i]
        s = sum(col)
        m = max(col)
        if float(s) == float(m):
            loc = np.where(col == m)[0][0]             
            val[gen_var(table)[i]] = table[loc, -1]
        else:
            val[gen_var(table)[i]] = 0 
            val['min'] = table[-1, -1] * -1
    return val


if __name__ == "__main__":
    # test
    m = gen_matrix(2,2)
    constrain(m,'2,-1,G,10')
    constrain(m,'1,1,L,20')
    obj(m,'5,10,0')
    print(maxz(m))     
    m = gen_matrix(2,4)
    constrain(m,'2,5,G,30')
    constrain(m,'-3,5,G,5')
    constrain(m,'8,3,L,85')
    constrain(m,'-9,7,L,42')
    obj(m,'2,7,0')
    print(minz(m))