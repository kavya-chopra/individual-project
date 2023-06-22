# code modified from https://medium.com/@jacob.d.moore1/coding-the-simplex-algorithm-from-scratch-using-python-and-numpy-93e3813e6e70 and
# https://github.com/jdmoore7/simplex_algorithm/blob/master/simplex.py

"""
Read-me:
Call functions in this order:
    problem = gen_matrix(v,c)
    constrain(problem, string)
    obj(problem, string)
    maxz(problem)
gen_matrix() produces a matrix to be given constraints and an objective function to maximize or minimize.
    It takes var (variable number) and cons (constraint number) as parameters.
    gen_matrix(2,3) will create a 4x7 matrix by design.
constrain() constrains the problem. It takes the problem as the first argument and a string as the second. The string should be
    entered in the form of 1,2,G,10 meaning 1(x1) + 2(x2) >= 10.
    Use 'L' for <= instead of 'G'
Use obj() only after entering all constraints, in the form of 1,2,0 meaning 1(x1) +2(x2) +0
    The final term is always reserved for a constant and 0 cannot be omitted.
Use maxz() to solve a maximization LP problem. Use minz() to solve a minimization problem.
Disclosure -- pivot() function, subcomponent of maxz() and minz(), has a couple bugs. So far, these have only occurred when
    minz() has been called.
"""

import numpy as np

# generates an empty matrix with adequate size for variables and constraints.
def gen_matrix(var, cons):
    tab = np.zeros((cons+1, var+cons+2))
    return tab

# checks the furthest right column for negative values ABOVE the last row. If negative values exist, another pivot is required.
def next_round_r(table):
    m = min(table[:-1, -1])
    return m < 0

# checks that the bottom row, excluding the final column, for negative values. If negative values exist, another pivot is required.
def next_round(table):
    lr = len(table[:, 0])
    m = min(table[lr-1, :-1])
    return m < 0

# Similar to next_round_r function, but returns row index of negative element in furthest right column
def find_neg_r(table):
    # lc = number of columns, lr = number of rows
    lc = len(table[0, :])
    # search every row (excluding last row) in final column for min value
    m = min(table[:-1, lc-1])
    if m <= 0:
        # n = row index of m location
        n = np.where(table[:-1, lc-1] == m)[0][0]
    else:
        n = None
    return n

#returns column index of negative element in bottom row
def find_neg(table):
    lr = len(table[:, 0])
    m = min(table[lr-1, :-1])
    if m <= 0:
        # n = row index for m
        n = np.where(table[lr-1, :-1] == m)[0][0]
    else:
        n = None
    return n

# locates pivot element in tableu to remove the negative element from the furthest right column.
def loc_piv_r(table):
        total = []
        # r = row index of negative entry
        r = find_neg_r(table)
        # finds all elements in row, r, excluding final column
        row = table[r, :-1]
        # finds minimum value in row (excluding the last column)
        m = min(row)
        # c = column index for minimum entry in row
        c = np.where(row == m)[0][0]
        # all elements in column
        col = table[:-1, c]
        # need to go through this column to find smallest positive ratio
        for i, b in zip(col,table[:-1, -1]):
            # i cannot equal 0 and b/i must be positive.
            if i**2 > 0 and b/i > 0:
                total.append(b/i)
            else:
                # placeholder for elements that did not satisfy the above requirements. Otherwise, our index number would be faulty.
                total.append(0)
        element = max(total)
        for t in total:
            if t > 0 and t < element:
                element = t
            else:
                continue

        index = total.index(element)
        return [index, c]
# similar process, returns a specific array element to be pivoted on.
def loc_piv(table):
    if next_round(table):
        total = []
        n = find_neg(table)
        for i,b in zip(table[:-1, n],table[:-1, -1]):
            if i**2 > 0 and b/i > 0:
                total.append(b/i)
            else:
                # placeholder for elements that did not satisfy the above requirements. Otherwise, our index number would be faulty.
                total.append(0)
        element = max(total)
        for t in total:
            if t > 0 and t < element:
                element = t
            else:
                continue

        index = total.index(element)
        return [index, n]

# Takes string input and returns a list of numbers to be arranged in tableu
def convert(eq):
    eq = eq.split(',')
    if 'G' in eq:
        g = eq.index('G')
        del eq[g]
        eq = [float(i)*-1 for i in eq]
        return eq
    if 'L' in eq:
        l = eq.index('L')
        del eq[l]
        eq = [float(i) for i in eq]
        return eq

# The final row of the tablue in a minimum problem is the opposite of a maximization problem so elements are multiplied by (-1)
def convert_min(table):
    table[-1,:-2] = [-1*i for i in table[-1,:-2]]
    table[-1,-1] = -1*table[-1,-1]
    return table

# generates x1,x2,...xn for the varying number of variables.
def gen_var(table):
    lc = len(table[0,:])
    lr = len(table[:,0])
    var = lc - lr -1
    v = []
    for i in range(var):
        v.append('x'+str(i+1))
    return v

# pivots the tableau such that negative elements are purged from the last row and last column
def pivot(row,col,table):
    # number of rows
    lr = len(table[:,0])
    # number of columns
    lc = len(table[0,:])
    t = np.zeros((lr,lc))
    pr = table[row,:]
    if table[row,col]**2>0: #new
        e = 1/table[row,col]
        r = pr*e
        for i in range(len(table[:,col])):
            k = table[i,:]
            c = table[i,col]
            if list(k) == list(pr):
                continue
            else:
                t[i,:] = list(k-r*c)
        t[row,:] = list(r)
        return t
    else:
        print('Cannot pivot on this element.')

# checks if there is room in the matrix to add another constraint
def add_cons(table):
    lr = len(table[:,0])
    # want to know IF at least 2 rows of all zero elements exist
    empty = []
    # iterate through each row
    for i in range(lr):
        total = 0
        for j in table[i,:]:
            # use squared value so (-x) and (+x) don't cancel each other out
            total += j**2
        if total == 0:
            # append zero to list ONLY if all elements in a row are zero
            empty.append(total)
    # There are at least 2 rows with all zero elements if the following is true
    if len(empty)>1:
        return True
    else:
        return False

# adds a constraint to the matrix
def constrain(table,eq):
    if add_cons(table) == True:
        lc = len(table[0,:])
        lr = len(table[:,0])
        var = lc - lr -1
        # set up counter to iterate through the total length of rows
        j = 0
        while j < lr:
            # Iterate by row
            row_check = table[j,:]
            # total will be sum of entries in row
            total = 0
            # Find first row with all 0 entries
            for i in row_check:
                total += float(i**2)
            if total == 0:
                # We've found the first row with all zero entries
                row = row_check
                break
            j +=1

        eq = convert(eq)
        i = 0
        # iterate through all terms in the constraint function, excluding the last
        while i<len(eq)-1:
            # assign row values according to the equation
            row[i] = eq[i]
            i +=1
        #row[len(eq)-1] = 1
        row[-1] = eq[-1]

        # add slack variable according to location in tableau.
        row[var+j] = 1
    else:
        print('Cannot add another constraint.')

# checks to determine if an objective function can be added to the matrix
def add_obj(table):
    lr = len(table[:,0])
    # want to know IF exactly one row of all zero elements exist
    empty = []
    # iterate through each row
    for i in range(lr):
        total = 0
        for j in table[i,:]:
            # use squared value so (-x) and (+x) don't cancel each other out
            total += j**2
        if total == 0:
            # append zero to list ONLY if all elements in a row are zero
            empty.append(total)
    # There is exactly one row with all zero elements if the following is true
    if len(empty)==1:
        return True
    else:
        return False

# adds the onjective functio nto the matrix.
def obj(table,eq):
    if add_obj(table)==True:
        eq = [float(i) for i in eq.split(',')]
        lr = len(table[:,0])
        row = table[lr-1,:]
        i = 0
    # iterate through all terms in the constraint function, excluding the last
        while i<len(eq)-1:
            # assign row values according to the equation
            row[i] = eq[i]*-1
            i +=1
        row[-2] = 1
        row[-1] = eq[-1]
    else:
        print('You must finish adding constraints before the objective function can be added.')

# solves maximization problem for optimal solution, returns dictionary w/ keys x1,x2...xn and max.
def maxz(table, output='summary'):
    while next_round_r(table)==True:
        table = pivot(loc_piv_r(table)[0],loc_piv_r(table)[1],table)
    while next_round(table)==True:
        table = pivot(loc_piv(table)[0],loc_piv(table)[1],table)

    lc = len(table[0,:])
    lr = len(table[:,0])
    var = lc - lr -1
    i = 0
    val = {}
    for i in range(var):
        col = table[:,i]
        s = sum(col)
        m = max(col)
        if float(s) == float(m):
            loc = np.where(col == m)[0][0]
            val[gen_var(table)[i]] = table[loc,-1]
        else:
            val[gen_var(table)[i]] = 0
    val['max'] = table[-1,-1]
    for k,v in val.items():
        val[k] = round(v,6)
    if output == 'table':
        return table
    else:
        return val

# solves minimization problems for optimal solution, returns dictionary w/ keys x1,x2...xn and min.
def minz(table, output='summary'):
    table = convert_min(table)

    while next_round_r(table)==True:
        table = pivot(loc_piv_r(table)[0],loc_piv_r(table)[1],table)
    while next_round(table)==True:
        table = pivot(loc_piv(table)[0],loc_piv(table)[1],table)

    lc = len(table[0,:])
    lr = len(table[:,0])
    var = lc - lr -1
    i = 0
    val = {}
    for i in range(var):
        col = table[:,i]
        s = sum(col)
        m = max(col)
        if float(s) == float(m):
            loc = np.where(col == m)[0][0]
            val[gen_var(table)[i]] = table[loc,-1]
        else:
            val[gen_var(table)[i]] = 0
    val['min'] = table[-1,-1]*-1
    for k,v in val.items():
        val[k] = round(v,6)
    if output == 'table':
        return table
    else:
        return val

if __name__ == "__main__":

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

# import numpy as np

# def gen_matrix(vars, cons):
#     # generate a matrix table with enough rows for each constraint and objective 
#     # function, and enough columns for variables and slack variables
#     turn_table = np.zeros((cons + 1, vars + cons + 2))
#     return turn_table

# def need_pivot_col(table):
#     # check if the table needs a pivot by checking last column
#     min_col = min(table[:-1, -1])
#     return min_col < 0

# def need_pivot_row(table):
#     # check if the table needs a pivot by checking last row
#     min_row = min(table[-1, :-1])
#     return min_row < 0

# def find_neg_col(table):
#     # find negative values in rightmost column
#     m = min(table[:-1, -1])
#     return np.where(table[:-1, -1] == m)[0][0] if m <= 0 else None

# def find_neg_row(table):
#     # find negative values in bottom row
#     lr = table.shape[0]
#     m = min(table[-1, :-1])
#     return np.where(table[-1, :-1] == m)[0][0] if m <= 0 else None

# def loc_piv_col(table):
#     #find pivot values in column
#     total = []        
#     r = find_neg_col(table)
#     row = table[r, :-1]
#     m = min(row)
#     c = np.where(row == m)[0][0]
#     col = table[:-1, c]
#     for i, b in zip(col, table[:-1, -1]):
#         if i**2 > 0 and b/i > 0:
#             total.append(b/i)
#         else:                
#             total.append(10000)
#     index = total.index(min(total))        
#     return [index, c]

# def loc_pivot_row(table):
#     #find pivot values in row
#     if need_pivot_col(table):
#         total = []
#         n = find_neg_row(table)
#         for i, b in zip(table[:-1, n],table[:-1, -1]):
#             if b/i > 0 and i**2 > 0:
#                 total.append(b/i)
#             else:
#                 total.append(10000)
#         index = total.index(min(total))
#         return [index,n]
    
# def pivot(row, col, table):
#     #pivot the table
#     t = np.zeros(table.shape)
#     pr = table[row, :]
#     if table[row, col]**2 > 0:
#         e = 1 / table[row, col]
#         r = pr * e
#         for i in range(len(table[:, col])):
#             k = table[i, :]
#             c = table[i, col]
#             if list(k) != list(pr):
#                 t[i,:] = list(k-r*c)
#         t[row, :] = list(r)
#         return t
#     else:
#         print('Cannot pivot on this element.')
        
# def convert(eq):
#     # convert user input string to float variables
#     # G means >= and L means <=
#     eq = eq.split(',')
#     if 'G' in eq:
#         g = eq.index('G')
#         del eq[g]
#         eq = [float(i) * -1 for i in eq]
#         return eq
#     if 'L' in eq:
#         l = eq.index('L')
#         del eq[l]
#         eq = [float(i) for i in eq]
#         return eq
    
# def convert_min(table):
#     # modify for minimisation problem
#     table[-1, :-2] = [-1 * i for i in table[-1, :-2]]
#     table[-1, -1] = -1 * table[-1, -1]    
#     return table

# def gen_var(table):
#     # generate only the required number of variables x1..xn
#     lc = table.shape[1]
#     lr = table.shape[0]
#     var = lc - lr -1
#     v = []
#     for i in range(var):
#         v.append('x'+ str(i + 1))
#     return v

# def add_cons(table):
#     # check if we can add constraints to the matrix
#     lr = table.shape[0]
#     empty = []
#     for i in range(lr):
#         total = 0
#         for j in table[i, :]:                       
#             total += j**2
#         if total == 0: 
#             empty.append(total)
#     return len(empty) > 1

# def constrain(table,eq):
#     # add a constraint to the matrix
#     if add_cons(table):
#         lc = table.shape[1]
#         lr = table.shape[0]
#         var = lc - lr -1      
#         j = 0
#         while j < lr:            
#             row_check = table[j, :]
#             total = 0
#             for i in row_check:
#                 total += float(i**2)
#             if total == 0:                
#                 row = row_check
#                 break
#             j += 1
#         eq = convert(eq)
#         i = 0
#         while i < len(eq) - 1:
#             row[i] = eq[i]
#             i += 1        
#         row[-1] = eq[-1]
#         row[var + j] = 1    
#     else:
#         print('Cannot add another constraint.')
        
# def add_obj(table):
#     # check if objective function can be added to tableau
#     lr = table.shape[0]
#     empty = []
#     for i in range(lr):
#         total = 0        
#         for j in table[i, :]:
#             total += j**2
#         if total == 0:
#             empty.append(total)    
#     return len(empty) == 1

# def obj(table,eq):
#     # add objective function to tableau once constraint functions have been added
#     if add_obj(table):
#         eq = [float(i) for i in eq.split(',')]
#         lr = table.shape[0]
#         row = table[lr - 1, :]
#         i = 0        
#         while i < len(eq)-1:
#             row[i] = eq[i] * -1
#             i += 1
#         row[-2] = 1
#         row[-1] = eq[-1]
#     else:
#         print('You must finish adding constraints before the objective function can be added.')
        
# def maxz(table):
#     # create maximization function
#     while need_pivot_col(table):
#         table = pivot(loc_piv_col(table)[0], loc_piv_col(table)[1],table)
#     while need_pivot_row(table):
#         table = pivot(loc_pivot_row(table)[0], loc_pivot_row(table)[1],table)        
#     lc = table.shape[1]
#     lr = table.shape[0]
#     var = lc - lr -1
#     i = 0
#     val = {}
#     for i in range(var):
#         col = table[:, i]
#         s = sum(col)
#         m = max(col)
#         if float(s) == float(m):
#             loc = np.where(col == m)[0][0]            
#             val[gen_var(table)[i]] = table[loc, -1]
#         else:
#             val[gen_var(table)[i]] = 0
#     val['max'] = table[-1, -1]
#     return val

# def minz(table):
#     # create minimization function
#     table = convert_min(table)
#     while need_pivot_col(table):
#         table = pivot(loc_piv_col(table)[0],loc_piv_col(table)[1],table)    
#     while need_pivot_row(table):
#         table = pivot(loc_pivot_row(table)[0],loc_pivot_row(table)[1],table)       
#     lc = table.shape[1]
#     lr = table.shape[0]
#     var = lc - lr - 1
#     i = 0
#     val = {}
#     for i in range(var):
#         col = table[:, i]
#         s = sum(col)
#         m = max(col)
#         if float(s) == float(m):
#             loc = np.where(col == m)[0][0]             
#             val[gen_var(table)[i]] = table[loc, -1]
#         else:
#             val[gen_var(table)[i]] = 0 
#             val['min'] = table[-1, -1] * -1
#     return val


# if __name__ == "__main__":
#     # test
#     m = gen_matrix(2,2)
#     constrain(m,'2,-1,G,10')
#     constrain(m,'1,1,L,20')
#     obj(m,'5,10,0')
#     print(maxz(m))     
#     m = gen_matrix(2,4)
#     constrain(m,'2,5,G,30')
#     constrain(m,'-3,5,G,5')
#     constrain(m,'8,3,L,85')
#     constrain(m,'-9,7,L,42')
#     obj(m,'2,7,0')
#     print(minz(m))